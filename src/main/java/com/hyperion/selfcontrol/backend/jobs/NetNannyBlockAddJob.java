package com.hyperion.selfcontrol.backend.jobs;

import com.hyperion.selfcontrol.Pair;
import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.Website;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyProfile;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class NetNannyBlockAddJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyBlockAddJob.class);

    public static List<Website> getCategoryList(NetNannyProfile profile, boolean forAllowedSection) {
        return profile.clickBlockAdd().map(page -> page.getItems(forAllowedSection)).orElse(Collections.emptyList());
    }

    public static Pair<List<Website>, List<Website>> getBlockAddLists(NetNannyProfile profile) {
        return profile.clickBlockAdd().map(page -> {
            List<Website> allowedItems = page.getItems(true);
            List<Website> blockedItems = page.getItems(false);
            return new Pair<>(allowedItems, blockedItems);
        }).orElse(new Pair<>(Collections.emptyList(), Collections.emptyList()));
    }

    private static boolean addItemInternal(NetNannyProfile profile, String website, boolean isAllow) {
        return profile.clickBlockAdd().map(page -> page.addItem(website, isAllow)).orElse(false);
    }

    private static boolean removeItemInternal(NetNannyProfile profile, String website, boolean isAllow) {
        return profile.clickBlockAdd().map(page -> page.removeItem(website, isAllow)).orElse(false);
    }

    public static boolean addItem(WebDriver driver, ConfigService configService, String website, boolean isAllow) {
        return NetNannyBaseJob.navigateToProfile(driver, configService).map(profile -> addItemInternal(profile, website, isAllow)).orElse(false);
    }

    public static boolean removeItem(WebDriver driver, ConfigService configService, String website, boolean isAllow) {
        return NetNannyBaseJob.navigateToProfile(driver, configService).map(profile -> removeItemInternal(profile, website, isAllow)).orElse(false);
    }
}
