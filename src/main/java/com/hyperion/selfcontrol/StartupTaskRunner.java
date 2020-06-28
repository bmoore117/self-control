package com.hyperion.selfcontrol;

import com.hyperion.selfcontrol.backend.BedtimeService;
import com.hyperion.selfcontrol.backend.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;


@Component
public class StartupTaskRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupTaskRunner.class);

    private final BedtimeService bedtimeService;
    private final JobRunner jobRunner;

    @Autowired
    public StartupTaskRunner(BedtimeService bedtimeService, JobRunner jobRunner) {
        this.bedtimeService = bedtimeService;
        this.jobRunner = jobRunner;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Unpacking password script");
        Resource resource = new ClassPathResource("changePassword.ps1");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Paths.get("changePassword.ps1"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error unpacking changePassword from classpath", e);
        }

        log.info("Unpacking internet script");
        resource = new ClassPathResource("toggleInternet.ps1");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Paths.get("toggleInternet.ps1"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error unpacking toggleInternet from classpath", e);
        }

        log.info("Unpacking ping script");
        resource = new ClassPathResource("ping.ps1");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Paths.get("ping.ps1"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error unpacking ping from classpath", e);
        }

        jobRunner.resetHallPassForTheWeekIfEligible();
        bedtimeService.reEnableInternetIfEligible();
        // todo is this needed if we have ping controller? It would seem safe enough to just wrap in a runAsync but is it really needed?
        CompletableFuture.runAsync(jobRunner::requeuePendingJobs);
    }
}
