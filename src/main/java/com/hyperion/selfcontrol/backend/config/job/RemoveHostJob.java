package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class RemoveHostJob extends OnlineJob {

    private String host;
    private boolean isAllow;

    public RemoveHostJob() {}

    public RemoveHostJob(LocalDateTime jobLaunchTime, String description, String host, boolean isAllow) {
        super(jobLaunchTime, description);
        this.host = host;
        this.isAllow = isAllow;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public void setAllow(boolean allow) {
        isAllow = allow;
    }
}
