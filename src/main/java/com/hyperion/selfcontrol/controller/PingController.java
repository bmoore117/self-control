package com.hyperion.selfcontrol.controller;

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

    @Autowired
    public PingController(JobRunner jobRunner) {
        this.jobRunner = jobRunner;
    }

    /**
     * Idea here was on ping to check state in config, and reset it if changed. Deprecated in favor of credential locking.
     * Now resurrected for timer adjustment after wake from sleep
     */
    @PostMapping(path = "/ping")
    public void checkStatus() {
        log.info("Received ping");
        jobRunner.onWake();

        /*List<AbstractFilterCategory> filterCategories = credentialService.getConfig().getState().getContentFilters().stream()
                .map(contentFilter -> new FilterCategory(contentFilter.getName(), contentFilter.getStatus()))
                .collect(Collectors.toList());

        List<AbstractFilterCategory> customFilterCategories = credentialService.getConfig().getState().getCustomContentFilters().stream()
                .map(contentFilter -> new CustomFilterCategory(contentFilter.getName(), contentFilter.getStatus()))
                .collect(Collectors.toList());

        Consumer<WebDriver> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                .ifPresent(profile -> {
                    profile.setForceSafeSearch(credentialService.getConfig().getState().isForceSafeSearch(), credentialService);
                    Optional<NetNannyFiltersPage> filtersPage = profile.clickMenu(CONTENT_FILTERS);
                    filtersPage.ifPresent(page -> {
                        page.findAndDo(credentialService, filterCategories, false);
                        page.close();
                    });
                    filtersPage = profile.clickMenu(CUSTOM_CONTENT_FILTERS);
                    filtersPage.ifPresent(page -> page.findAndDo(credentialService, customFilterCategories, false));
                });
        Runnable withDriver = Utils.composeWithDriver(function);
        withDriver.run();*/
    }
}
