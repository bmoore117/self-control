package com.hyperion.selfcontrol.backend.jobs;

import com.hyperion.selfcontrol.Pair;
import com.hyperion.selfcontrol.backend.Website;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyProfile;
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

    public static boolean addItem(NetNannyProfile profile, String website, boolean isAllow) {
        return profile.clickBlockAdd().map(page -> page.addItem(website, isAllow)).orElse(false);
    }

    public static boolean removeItem(NetNannyProfile profile, String website, boolean isAllow) {
        return profile.clickBlockAdd().map(page -> page.removeItem(website, isAllow)).orElse(false);
    }
}
