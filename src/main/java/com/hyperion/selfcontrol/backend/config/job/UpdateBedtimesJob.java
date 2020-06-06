package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;

import java.time.LocalDateTime;

public class UpdateBedtimesJob extends OfflineJob {

    private static final String BEDTIMES = "bedtimes";

    public UpdateBedtimesJob() {}

    public UpdateBedtimesJob(LocalDateTime jobLaunchTime, String description, Bedtimes bedtimes) {
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
