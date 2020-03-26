package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import com.hyperion.selfcontrol.backend.FilterCategory;
import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NetNannyStatusJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    public static List<FilterCategory> getNetNannyStatuses(NetNannyProfile profile) {
        Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu("net nanny content filters");
        log.info("Opening restrictions menu");
        if (!filtersOpt.isPresent()) {
            log.error("Restrictions menu not present");
            return Collections.emptyList();
        }

        NetNannyFiltersPage filtersPage = filtersOpt.get();
        return filtersPage.getStatuses();
    }

    public static List<CustomFilterCategory> getNetNannyCustomStatuses(NetNannyProfile profile) {
        Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu("custom content filters");
        log.info("Opening restrictions menu");
        if (!filtersOpt.isPresent()) {
            log.error("Restrictions menu not present");
            return Collections.emptyList();
        }

        NetNannyFiltersPage filtersPage = filtersOpt.get();
        return filtersPage.getCustomStatuses();
    }
}
