package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public abstract class OfflineJob extends Job {
    public OfflineJob() {}
    public OfflineJob(LocalDateTime jobLaunchTime, String description) {
        super(jobLaunchTime, description);
    }
}
