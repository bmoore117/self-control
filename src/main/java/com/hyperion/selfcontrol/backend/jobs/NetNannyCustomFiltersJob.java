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

    public static boolean saveCustomFilters(WebDriver driver, CustomFilterCategory customFilterCategory, ConfigService configService) {
        Optional<Boolean> aBoolean = NetNannyBaseJob.navigateToProfile(driver, configService)
                .flatMap(profile -> profile.clickFiltersMenu(CUSTOM_CONTENT_FILTERS))
                .map(filtersPage -> filtersPage.upsertCustomFilterCategory(customFilterCategory));
        return aBoolean.orElse(false);
    }

    public static void deleteCategory(WebDriver driver, CustomFilterCategory category, ConfigService configService) {
        Optional<Boolean> aBoolean = NetNannyBaseJob.navigateToProfile(driver, configService)
                .flatMap(profile -> profile.clickFiltersMenu(CUSTOM_CONTENT_FILTERS))
                .map(filtersPage -> filtersPage.deleteCustomFilterCategory(category));
        boolean result = aBoolean.orElse(false);
        log.info("Custom filter category {} deletion status: {}", category.getName(), result);
    }
}
