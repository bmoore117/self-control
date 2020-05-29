package com.hyperion.selfcontrol.backend;

import com.hyperion.selfcontrol.backend.config.job.*;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBlockAddJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyCustomFiltersJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyToggleStatusJob;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);
    private final ConfigService configService;
    private final BedtimeService bedtimeService;

    private static List<CustomTimerTask> tasks = new LinkedList<>();

    @Autowired
    public JobRunner(ConfigService configService, BedtimeService bedtimeService) {
        this.configService = configService;
        this.bedtimeService = bedtimeService;
    }

    class CustomTimerTask extends TimerTask {

        private final LocalDateTime runAt;
        private final String jobDescription;

        public CustomTimerTask(LocalDateTime runAt, String jobDescription) {
            this.runAt = runAt;
            this.jobDescription = jobDescription;
        }

        public LocalDateTime getRunAt() {
            return runAt;
        }

        public String getJobDescription() {
            return jobDescription;
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
            if (!task.getRunAt().isAfter(LocalDateTime.now())) {
                CustomTimerTask newTask = new CustomTimerTask(task.getRunAt(), task.getJobDescription());
                remainingTasks.add(newTask);
                long epochMilli = task.getRunAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long timerDuration = epochMilli - System.currentTimeMillis();
                Timer timer = new Timer(task.getJobDescription());
                timer.schedule(newTask, timerDuration);
            }
        }
        tasks = remainingTasks;
        runReadyJobs();
    }

    public void queueJob(Job job) {
        log.info("Scheduling job: {}", job.getJobDescription());
        configService.getConfig().getPendingJobs().add(job);
        configService.writeFile();
        CustomTimerTask task = new CustomTimerTask(job.getJobLaunchTime(), job.getJobDescription());
        Timer timer = new Timer(job.getJobDescription());
        timer.schedule(task, configService.getDelayMillis());
        tasks.add(task);
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
            RuntimeException e = null;
            boolean result = true;
            try {
                result = runJob(job);
            } catch (RuntimeException ex) {
                e = ex;
                log.error("Error running job, moving to manual retry queue", ex);
                configService.getConfig().getRetryJobs().add(job);
            }

            if (e == null && result) {
                configService.getConfig().getPendingJobs().remove(job);
            } else if (!result) {
                log.error("Job {} did not return success, moving to retry queue", job.getJobDescription());
                configService.getConfig().getRetryJobs().add(job);
            }
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

    private void retryFailedJobsInternal(boolean writeFile) {
        for (Job job : configService.getConfig().getRetryJobs()) {
            log.info("Retrying previously errored job {}", job.getJobDescription());
            RuntimeException e = null;
            boolean result = true;
            try {
                result = runJob(job);
            } catch (RuntimeException ex) {
                e = ex;
                log.error("Retry of job failed, leaving in retry queue");
            }

            if (e == null && result) {
                log.info("Job succeeded, removing from queue");
                configService.getConfig().getRetryJobs().remove(job);
            } else if (!result) {
                log.error("Retry of job {} did not return success, leaving in retry queue", job.getJobDescription());
            }
        }

        if (writeFile) {
            configService.writeFile();
        }
    }

    public boolean runJob(Job job) {
        log.info("Starting job {}", job.getJobDescription());
        Boolean result;
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
                log.info("Job {} completed successfully", job.getJobDescription());
            } else {
                UpdateBedtimesJob updateBedtimesJob = (UpdateBedtimesJob) job;
                configService.getConfig().setBedtimes(updateBedtimesJob.getBedtimes());
                configService.writeFile();
                bedtimeService.scheduleToday(updateBedtimesJob.getBedtimes());
            }
            result = true;
        }

        return result;
    }
}
