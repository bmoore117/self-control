package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class ToggleInternetJob extends OfflineJob {

    private static final String INTERNET_CUTOFF_TIME = "internetCutoffTime";

    public ToggleInternetJob() {}

    public ToggleInternetJob(LocalDateTime jobLaunchTime, String description) {
        super(jobLaunchTime, description);
    }
}
