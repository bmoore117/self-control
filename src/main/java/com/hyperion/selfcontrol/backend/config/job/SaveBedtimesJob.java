package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;

import java.time.LocalDateTime;

public class SaveBedtimesJob extends OfflineJob {

    private static final String BEDTIMES = "bedtimes";

    public SaveBedtimesJob() {}

    public SaveBedtimesJob(LocalDateTime jobLaunchTime, String description, Bedtimes bedtimes) {
        super(jobLaunchTime, description);
        data.put(BEDTIMES, bedtimes);
    }

    public Bedtimes getBedtimes() {
        return get(BEDTIMES, Bedtimes.class);
    }

    public void setBedtimes(Bedtimes bedtimes) {
        data.put(BEDTIMES, bedtimes);
    }
}
