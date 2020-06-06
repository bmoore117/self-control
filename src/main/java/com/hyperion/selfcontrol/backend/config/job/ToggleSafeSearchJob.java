package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class ToggleSafeSearchJob extends OnlineJob {

    private static final String ON = "on";

    public ToggleSafeSearchJob() {
    }

    public ToggleSafeSearchJob(LocalDateTime jobLaunchTime, String description, boolean on) {
        super(jobLaunchTime, description);
        data.put(ON, on);
    }

    public Boolean isOn() {
        return get(ON, Boolean.class);
    }

    public void setOn(boolean on) {
        data.put(ON, on);
    }
}
