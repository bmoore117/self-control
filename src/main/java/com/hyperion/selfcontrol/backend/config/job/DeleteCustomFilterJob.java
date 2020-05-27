package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;

import java.time.LocalDateTime;

public class DeleteCustomFilterJob extends Job {

    private CustomFilterCategory filterToDelete;

    public DeleteCustomFilterJob() {}

    public DeleteCustomFilterJob(LocalDateTime jobLaunchTime, String jobDescription, CustomFilterCategory filterToDelete) {
        super(jobLaunchTime, jobDescription);
        this.filterToDelete = filterToDelete;
    }

    public CustomFilterCategory getFilterToDelete() {
        return filterToDelete;
    }

    public void setFilterToDelete(CustomFilterCategory filterToDelete) {
        this.filterToDelete = filterToDelete;
    }
}
