package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public abstract class Job {

    private LocalDateTime jobLaunchTime;
    private String jobDescription;

    public Job() {}

    public Job(LocalDateTime jobLaunchTime, String jobDescription) {
        this.jobLaunchTime = jobLaunchTime;
        this.jobDescription = jobDescription;
    }

    public LocalDateTime getJobLaunchTime() {
        return jobLaunchTime;
    }

    public String getJobDescription() {
        return jobDescription;
    }
}
