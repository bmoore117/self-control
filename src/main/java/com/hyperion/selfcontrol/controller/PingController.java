package com.hyperion.selfcontrol.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    public static final Logger log = LoggerFactory.getLogger(PingController.class);

    @PostMapping(path = "/ping")
    public void checkStatus() {
        log.info("Received ping");
    }
}
