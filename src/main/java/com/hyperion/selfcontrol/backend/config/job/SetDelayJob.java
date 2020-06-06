package com.hyperion.selfcontrol.backend.config.job;

import java.time.LocalDateTime;

public class SetDelayJob extends OfflineJob {

    private static final String DELAY = "delay";

    public SetDelayJob() {
    }

    public SetDelayJob(LocalDateTime jobLaunchTime, String jobDescription, long delay) {
        super(jobLaunchTime, jobDescription);
        data.put("delay", delay);
    }

    public Long getDelay() {
        Object o = data.get(DELAY);
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        } else {
            return (Long) o;
        }
    }

    public void setDelay(Long delay) {
        data.put(DELAY, delay);
    }
}
