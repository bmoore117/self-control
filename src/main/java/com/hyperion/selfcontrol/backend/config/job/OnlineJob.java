package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public abstract class OnlineJob extends Job {
    public OnlineJob() {}
    public OnlineJob(LocalDateTime jobLaunchTime, String description) {}
}
