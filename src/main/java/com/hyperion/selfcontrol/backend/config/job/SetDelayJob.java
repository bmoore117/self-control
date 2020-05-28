package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class SetDelayJob extends OfflineJob {

    private long delay;

    public SetDelayJob() {
    }

    public SetDelayJob(LocalDateTime jobLaunchTime, String jobDescription, long delay) {
        super(jobLaunchTime, jobDescription);
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
