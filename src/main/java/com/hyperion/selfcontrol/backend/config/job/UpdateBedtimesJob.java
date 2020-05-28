package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;

import java.time.LocalDateTime;

public class UpdateBedtimesJob extends OfflineJob {

    private Bedtimes bedtimes;

    public UpdateBedtimesJob() {}

    public UpdateBedtimesJob(LocalDateTime jobLaunchTime, String description, Bedtimes bedtimes) {
        super(jobLaunchTime, description);
        this.bedtimes = bedtimes;
    }

    public Bedtimes getBedtimes() {
        return bedtimes;
    }

    public void setBedtimes(Bedtimes bedtimes) {
        this.bedtimes = bedtimes;
    }
}
