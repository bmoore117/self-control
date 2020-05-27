package com.hyperion.selfcontrol.backend;

import com.hyperion.selfcontrol.backend.config.job.*;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBlockAddJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyCustomFiltersJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannySetCategoryJob;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);
    private final ConfigService configService;

    @Autowired
    public JobRunner(ConfigService configService) {
        this.configService = configService;
    }

    public boolean queueJob(Job job) {
        configService.getConfig().getPendingJobs().add(job);
        configService.writeFile();
        return true;
    }

    public void runReadyJobs() {
        configService.getConfig().getPendingJobs().stream()
                .filter(job -> !job.getJobLaunchTime().isAfter(LocalDateTime.now()))
                .forEach(this::runJob);
    }

    public boolean runJob(Job job) {
        log.info("Starting job {}", job.getJobDescription());
        Boolean result;
        Function<WebDriver, Boolean> function;
        if (job instanceof AddHostJob) {
            AddHostJob addJob = (AddHostJob) job;
            function = driver -> NetNannyBlockAddJob.addItem(driver, configService, addJob.getHostToAdd(), addJob.isAllow());
        } else if (job instanceof RemoveHostJob) {
            RemoveHostJob removeJob = (RemoveHostJob) job;
            function = driver -> NetNannyBlockAddJob.removeItem(driver, configService, removeJob.getHost(), removeJob.isAllow());
        } else if (job instanceof DeleteCustomFilterJob) {
            DeleteCustomFilterJob deleteJob = (DeleteCustomFilterJob) job;
            function = driver -> NetNannyCustomFiltersJob.deleteCategory(driver, deleteJob.getFilterToDelete(), configService);
        } else if (job instanceof ToggleFilterJob) {
            ToggleFilterJob toggleJob = (ToggleFilterJob) job;
            function = driver -> NetNannySetCategoryJob.setCategories(driver, configService, toggleJob.getMenuItem(), toggleJob.getFilterCategories());
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
        return result;
    }
}
