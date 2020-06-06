package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;

import java.time.LocalDateTime;

public class DeleteCustomFilterJob extends OnlineJob {

    private static final String FILTER_TO_DELETE = "filterToDelete";

    public DeleteCustomFilterJob() {}

    public DeleteCustomFilterJob(LocalDateTime jobLaunchTime, String jobDescription, CustomFilterCategory filterToDelete) {
        super(jobLaunchTime, jobDescription);
        data.put(FILTER_TO_DELETE, filterToDelete);
    }

    public CustomFilterCategory getFilterToDelete() {
        return get(FILTER_TO_DELETE, CustomFilterCategory.class);
    }

    public void setFilterToDelete(CustomFilterCategory filterToDelete) {
        data.put(FILTER_TO_DELETE, filterToDelete);
    }
}
