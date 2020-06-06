package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class RemoveHostJob extends OnlineJob {

    private static final String HOST = "host";
    private static final String IS_ALLOW = "isAllow";

    public RemoveHostJob() {}

    public RemoveHostJob(LocalDateTime jobLaunchTime, String description, String host, boolean isAllow) {
        super(jobLaunchTime, description);
        data.put(HOST, host);
        data.put(IS_ALLOW, isAllow);
    }

    public String getHost() {
        return get(HOST, String.class);
    }

    public void setHost(String host) {
        data.put(HOST, host);
    }

    public Boolean isAllow() {
        return get(IS_ALLOW, Boolean.class);
    }

    public void setAllow(boolean allow) {
        data.put(IS_ALLOW, allow);
    }
}
