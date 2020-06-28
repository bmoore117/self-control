package com.hyperion.selfcontrol.controller;

import com.hyperion.selfcontrol.backend.BedtimeService;
import com.hyperion.selfcontrol.backend.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
public class PingController {

    public static final Logger log = LoggerFactory.getLogger(PingController.class);

    private final JobRunner jobRunner;
    private final BedtimeService bedtimeService;

    @Autowired
    public PingController(JobRunner jobRunner, BedtimeService bedtimeService) {
        this.jobRunner = jobRunner;
        this.bedtimeService = bedtimeService;
    }

    @PostMapping(path = "/ping")
    public void checkStatus() {
        log.info("Received ping");
        jobRunner.resetHallPassForTheWeekIfEligible();
        bedtimeService.reEnableInternetIfEligible();
        jobRunner.onWake();
    }
}
