package com.hyperion.selfcontrol.backend.jobs;

import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage.CUSTOM_CONTENT_FILTERS;

public class NetNannyCustomFiltersJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyCustomFiltersJob.class);

    public static boolean saveCustomFilter(WebDriver driver, ConfigService configService, CustomFilterCategory customFilterCategory) {
        Optional<Boolean> aBoolean = NetNannyBaseJob.navigateToProfile(driver, configService)
                .flatMap(profile -> profile.clickFiltersMenu(CUSTOM_CONTENT_FILTERS))
                .map(filtersPage -> filtersPage.upsertCustomFilterCategory(customFilterCategory));
        return aBoolean.orElse(false);
    }

    public static boolean deleteCategory(WebDriver driver, CustomFilterCategory category, ConfigService configService) {
        Optional<Boolean> aBoolean = NetNannyBaseJob.navigateToProfile(driver, configService)
                .flatMap(profile -> profile.clickFiltersMenu(CUSTOM_CONTENT_FILTERS))
                .map(filtersPage -> filtersPage.deleteCustomFilterCategory(category));
        return aBoolean.orElse(false);
    }
}
