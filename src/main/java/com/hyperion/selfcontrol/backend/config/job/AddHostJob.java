package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class AddHostJob extends OnlineJob {

    private static final String HOST_TO_ADD = "hostToAdd";
    private static final String IS_ALLOW = "isAllow";

    public AddHostJob() {}

    public AddHostJob(LocalDateTime jobLaunchTime, String description, String hostToAdd, boolean isAllow) {
        super(jobLaunchTime, description);
        data.put(HOST_TO_ADD, hostToAdd);
        data.put(IS_ALLOW, isAllow);
    }

    public String getHostToAdd() {
        return get(HOST_TO_ADD, String.class);
    }

    public void setHostToAdd(String hostToAdd) {
        data.put(HOST_TO_ADD, hostToAdd);
    }

    public Boolean isAllow() {
        return get(IS_ALLOW, Boolean.class);
    }

    public void setAllow(boolean allow) {
        data.put(IS_ALLOW, allow);
    }
}

