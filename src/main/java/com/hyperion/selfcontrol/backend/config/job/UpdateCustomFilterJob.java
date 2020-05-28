package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;

import java.time.LocalDateTime;

public class UpdateCustomFilterJob extends OnlineJob {

    private CustomFilterCategory customFilterCategory;

    public UpdateCustomFilterJob(LocalDateTime jobLaunchTime, String jobDescription, CustomFilterCategory customFilterCategory) {
        super(jobLaunchTime, jobDescription);
        this.customFilterCategory = customFilterCategory;
    }

    public CustomFilterCategory getCustomFilterCategory() {
        return customFilterCategory;
    }

    public void setCustomFilterCategory(CustomFilterCategory customFilterCategory) {
        this.customFilterCategory = customFilterCategory;
    }
}
