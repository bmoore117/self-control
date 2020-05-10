package com.hyperion.selfcontrol.controller;

import com.hyperion.selfcontrol.backend.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    public static final Logger log = LoggerFactory.getLogger(PingController.class);

    @Autowired
    private ConfigService configService;

    /**
     * Idea here was on ping to check state in config, and reset it if changed. Deprecated in favor of credential locking.
     */
    @PostMapping(path = "/ping")
    public void checkStatus() {
        log.info("Received ping");

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
