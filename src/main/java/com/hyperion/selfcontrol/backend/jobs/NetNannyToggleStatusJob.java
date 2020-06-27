package com.hyperion.selfcontrol.backend.jobs;

import com.hyperion.selfcontrol.backend.AbstractFilterCategory;
import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyProfile;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class NetNannyToggleStatusJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    private static Optional<NetNannyProfile> setCategoriesInternal(NetNannyProfile profile, ConfigService configService, String menuItem, List<AbstractFilterCategory> filterCategories) {
        Optional<NetNannyFiltersPage> filtersOpt = profile.clickFiltersMenu(menuItem);
        log.info("Opening restrictions menu");
        if (!filtersOpt.isPresent()) {
            log.error("Restrictions menu not present");
            return Optional.empty();
        }

        NetNannyFiltersPage filtersPage = filtersOpt.get();
        filtersPage.findAndDo(configService, filterCategories, true);
        return Optional.of(filtersPage.close());
    }

    public static boolean setCategories(WebDriver driver, ConfigService configService, String menuItem, List<AbstractFilterCategory> filterCategories) {
       return NetNannyBaseJob.navigateToProfile(driver, configService)
               .map(profile -> profile.clickFiltersMenu(menuItem)
                       .map(filtersPage -> filtersPage.findAndDo(configService, filterCategories, true))
                       .orElse(false))
               .orElse(false);
    }

    public static boolean toggleSafeSearch(WebDriver driver, ConfigService configService, boolean on) {
        return NetNannyBaseJob.navigateToProfile(driver, configService)
                .map(profile -> profile.setForceSafeSearch(on, configService)).orElse(false);
    }
}
