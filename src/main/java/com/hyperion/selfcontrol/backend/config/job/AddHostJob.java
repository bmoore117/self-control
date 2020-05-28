package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class AddHostJob extends OnlineJob {

    private String hostToAdd;
    private boolean isAllow;

    public AddHostJob() {}

    public AddHostJob(LocalDateTime jobLaunchTime, String description, String hostToAdd, boolean isAllow) {
        super(jobLaunchTime, description);
        this.hostToAdd = hostToAdd;
        this.isAllow = isAllow;
    }

    public String getHostToAdd() {
        return hostToAdd;
    }

    public void setHostToAdd(String hostToAdd) {
        this.hostToAdd = hostToAdd;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public void setAllow(boolean allow) {
        isAllow = allow;
    }
}

