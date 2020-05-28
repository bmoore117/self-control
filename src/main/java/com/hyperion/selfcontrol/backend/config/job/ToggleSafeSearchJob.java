package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class ToggleSafeSearchJob extends OnlineJob {

    private boolean on;

    public ToggleSafeSearchJob() {
    }

    public ToggleSafeSearchJob(LocalDateTime jobLaunchTime, String description, boolean on) {
        super(jobLaunchTime, description);
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
