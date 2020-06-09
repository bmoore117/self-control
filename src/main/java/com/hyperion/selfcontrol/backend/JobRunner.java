package com.hyperion.selfcontrol.backend;

import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;
import com.hyperion.selfcontrol.backend.config.job.*;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBlockAddJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyCustomFiltersJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyToggleStatusJob;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);
    public static final String TOGGLE_INTERNET_OFF = "Toggle internet off";
    private final ConfigService configService;

    private static List<CustomTimerTask> tasks = new LinkedList<>();

    @Autowired
    public JobRunner(ConfigService configService) {
        this.configService = configService;
    }

    class CustomTimerTask extends TimerTask {

        private final LocalDateTime runAt;
        private final String jobDescription;
        private final UUID jobId;

        public CustomTimerTask(LocalDateTime runAt, String jobDescription, UUID jobId) {
            this.runAt = runAt;
            this.jobDescription = jobDescription;
            this.jobId = jobId;
        }

        public LocalDateTime getRunAt() {
            return runAt;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public UUID getJobId() {
            return jobId;
        }

        @Override
        public void run() {
            // this is added so we don't block the Timer thread, since runReadyJobs may take a while to complete and
            // is synchronized
            CompletableFuture.runAsync(() -> {
                runReadyJobs();
                tasks.remove(this);
            });
        }
    }

    public void onWake() {
        log.info("Adjusting job timers in onWake");
        List<CustomTimerTask> remainingTasks = new LinkedList<>();
        for (CustomTimerTask task : tasks) {
            task.cancel(); // all existing timers are invalid, time has shifted under them by however much we slept for
            if (task.getRunAt().isAfter(LocalDateTime.now())) {
                CustomTimerTask newTask = new CustomTimerTask(task.getRunAt(), task.getJobDescription(), task.getJobId());
                remainingTasks.add(newTask);
                long epochMilli = task.getRunAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long timerDuration = epochMilli - System.currentTimeMillis();
                Timer timer = new Timer(task.getJobDescription());
                log.info("Task {} scheduled to be run at {}, rescheduling with timer of {}ms", task.getJobDescription(), task.getRunAt(), timerDuration);
                timer.schedule(newTask, timerDuration);
            }
        }
        tasks = remainingTasks;
        runReadyJobs();
    }

    public void queueJob(Job job) {
        queueJobInternal(job, true);
    }

    private void queueJobInternal(Job job, boolean writeFile) {
        log.info("Scheduling job: {}", job.getJobDescription());
        configService.getConfig().getPendingJobs().add(job.dehydrateJob());
        if (writeFile) {
            configService.writeFile();
        }
        CustomTimerTask task = new CustomTimerTask(job.getJobLaunchTime(), job.getJobDescription(), job.getId());
        Timer timer = new Timer(job.getJobDescription());
        long epochMilli = task.getRunAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long timerDuration = epochMilli - System.currentTimeMillis();
        if (timerDuration < 0) {
            timerDuration = 0;
        }
        timer.schedule(task, timerDuration);
        tasks.add(task);
    }

    public void requeuePendingJobs() {
        for (Job job : configService.getConfig().getPendingJobs()) {
            if (LocalDateTime.now().isBefore(job.getJobLaunchTime())) {
                queueJobInternal(job, false);
            }
        }
        runReadyJobs();
    }

    /*
        Of note, if other jobs become ready while the current set of ready jobs is still running, we expect to still
        catch it because of the fact that there will be blocking threads calling this method waiting in line for their
        turn to run it, due to the job-timer association
     */
    public synchronized void runReadyJobs() {
        retryFailedJobsInternal(false);

        List<Job> jobs = configService.getConfig().getPendingJobs().stream()
                .filter(job -> !job.getJobLaunchTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        for (Job job : jobs) {
            RuntimeException e;
            try {
                runJob(job);
            } catch (RuntimeException ex) {
                e = ex;
                if (!configService.getConfig().getRetryJobs().contains(job)) {
                    log.error("Job " + job.getJobDescription() + " did not return success, moving to retry queue", e);
                    configService.getConfig().getRetryJobs().add(job);
                }
            }

            // this is safe despite being in a for loop because it's not the collection we are iterating over
            configService.getConfig().getPendingJobs().remove(job);
        }

        configService.writeFile();
    }

    public void retryFailedJobs() {
        retryFailedJobsInternal(true);
    }

    public void deleteFailedJobs() {
        configService.getConfig().getRetryJobs().clear();
        configService.writeFile();
    }

    public void cancelPendingJobs() {
        Iterator<CustomTimerTask> it = tasks.iterator();
        List<UUID> jobIds = new LinkedList<>();
        while (it.hasNext()) {
            CustomTimerTask task = it.next();
            if (!task.getJobDescription().equals(TOGGLE_INTERNET_OFF)) {
                task.cancel();
                jobIds.add(task.getJobId());
                it.remove();
            }
        }
        jobIds.forEach(id -> configService.getConfig().getPendingJobs().removeIf(job -> !job.getId().equals(id)));
        configService.writeFile();
    }

    private void cancelPendingToggleInternetJobs() {
        Iterator<CustomTimerTask> it = tasks.iterator();
        List<UUID> jobIds = new LinkedList<>();
        while (it.hasNext()) {
            CustomTimerTask task = it.next();
            if (task.getJobDescription().equals(TOGGLE_INTERNET_OFF)) {
                task.cancel();
                jobIds.add(task.getJobId());
                it.remove();
            }
        }
        jobIds.forEach(id -> configService.getConfig().getPendingJobs().removeIf(job -> job.getId().equals(id)));
        configService.writeFile();
    }

    private void retryFailedJobsInternal(boolean writeFile) {
        Iterator<Job> it = configService.getConfig().getRetryJobs().iterator();
        while (it.hasNext()) {
            Job job = it.next();
            log.info("Retrying previously errored job {}", job.getJobDescription());
            RuntimeException e = null;
            boolean result = true;
            try {
                result = runJob(job);
            } catch (RuntimeException ex) {
                e = ex;
                log.error("Retry of job failed, leaving in retry queue", e);
            }

            if (e == null && result) {
                log.info("Job succeeded, removing from queue");
                it.remove();
            }
        }

        if (writeFile) {
            configService.writeFile();
        }
    }

    public static Job convertIfNecessary(Job incoming) {
        if (incoming == null) {
            return null;
        }

        if (incoming.getClass().equals(Job.class)) {
            // if this is the case then this class has been rehydrated from concentrate, and needs to be cast
            try {
                Constructor<? extends Job> constructor = incoming.getConcreteClass().getConstructor();
                Job job = constructor.newInstance();
                job.setJobDescription(incoming.getJobDescription());
                job.setJobLaunchTime(incoming.getJobLaunchTime());
                job.setId(incoming.getId());
                job.setConcreteClass(incoming.getConcreteClass());
                job.setData(incoming.getData());
                return job;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("This should not have happened, does your job class follow the established pattern?", e);
                return null;
            }
        } else {
            return incoming;
        }
    }

    public boolean runJob(Job incoming) {
        log.info("Starting job {}", incoming.getJobDescription());
        Boolean result;
        Job job = convertIfNecessary(incoming);
        if (job instanceof OnlineJob) {
            Function<WebDriver, Boolean> function;
            if (job instanceof AddHostJob) {
                AddHostJob addJob = (AddHostJob) job;
                function = driver -> NetNannyBlockAddJob.addItem(driver, configService, addJob.getHostToAdd(), addJob.isAllow());
            } else if (job instanceof DeleteCustomFilterJob) {
                DeleteCustomFilterJob deleteJob = (DeleteCustomFilterJob) job;
                function = driver -> NetNannyCustomFiltersJob.deleteCategory(driver, deleteJob.getFilterToDelete(), configService);
            } else if (job instanceof RemoveHostJob) {
                RemoveHostJob removeJob = (RemoveHostJob) job;
                function = driver -> NetNannyBlockAddJob.removeItem(driver, configService, removeJob.getHost(), removeJob.isAllow());
            } else if (job instanceof ToggleFilterJob) {
                ToggleFilterJob toggleJob = (ToggleFilterJob) job;
                function = driver -> NetNannyToggleStatusJob.setCategories(driver, configService, toggleJob.getMenuItem(), toggleJob.getFilterCategories());
            } else if (job instanceof ToggleSafeSearchJob) {
                ToggleSafeSearchJob toggleJob = (ToggleSafeSearchJob) job;
                function = driver -> NetNannyToggleStatusJob.toggleSafeSearch(driver, configService, toggleJob.isOn());
            } else {
                UpdateCustomFilterJob updateJob = (UpdateCustomFilterJob) job;
                function = driver -> NetNannyCustomFiltersJob.saveCustomFilter(driver, configService, updateJob.getCustomFilterCategory());
            }

            Supplier<Boolean> supplier = Utils.composeWithDriver(function);
            result = supplier.get();
            if (result) {
                log.info("Job {} completed successfully", job.getJobDescription());
            } else {
                log.error("Job {} did not complete successfully", job.getJobDescription());
            }
        } else {
            if (job instanceof SetDelayJob) {
                SetDelayJob delayJob = (SetDelayJob) job;
                configService.setDelay(delayJob.getDelay());
                configService.writeFile();
                log.info("Job {} completed successfully", job.getJobDescription());
            } else if (job instanceof SaveBedtimesJob) {
                SaveBedtimesJob saveBedtimesJob = (SaveBedtimesJob) job;
                Bedtimes bedtimes = saveBedtimesJob.getBedtimes();
                configService.getConfig().setBedtimes(bedtimes);
                configService.writeFile();

                // if we have set today == null, and there were previous timers extant, cancel. If we're updating a
                // timer, also cancel
                cancelPendingToggleInternetJobs();

                LocalTime today = bedtimes.today();
                if (today != null) {
                    LocalDate date = LocalDate.now();
                    LocalDateTime launchTime = date.atTime(today);
                    ToggleInternetJob toggleInternetJob = new ToggleInternetJob(launchTime, TOGGLE_INTERNET_OFF);
                    queueJob(toggleInternetJob);
                }
            } else {
                // job is an instance of ToggleInternetJob
                LocalDate date = LocalDate.now();
                DayOfWeek dow = date.getDayOfWeek();

                // if we're still on the same day and haven't slept or been powered off thru til next day
                if (dow.equals(job.getJobLaunchTime().getDayOfWeek())) {
                    Utils.toggleInternet(false);
                }
            }
            result = true;
        }

        return result;
    }
}
