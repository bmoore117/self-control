package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;

import java.time.LocalDateTime;

public class UpdateCustomFilterJob extends OnlineJob {

    private static final String CUSTOM_FILTER_CATEGORY = "customFilterCategory";

    public UpdateCustomFilterJob() {}

    public UpdateCustomFilterJob(LocalDateTime jobLaunchTime, String jobDescription, CustomFilterCategory customFilterCategory) {
        super(jobLaunchTime, jobDescription);
        data.put(CUSTOM_FILTER_CATEGORY, customFilterCategory);
    }

    public CustomFilterCategory getCustomFilterCategory() {
        return get(CUSTOM_FILTER_CATEGORY, CustomFilterCategory.class);
    }

    public void setCustomFilterCategory(CustomFilterCategory customFilterCategory) {
        data.put(CUSTOM_FILTER_CATEGORY, customFilterCategory);
    }
}
