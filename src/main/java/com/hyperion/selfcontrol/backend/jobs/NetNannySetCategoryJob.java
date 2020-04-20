package com.hyperion.selfcontrol.backend.jobs;

import com.hyperion.selfcontrol.backend.AbstractFilterCategory;
import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class NetNannySetCategoryJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    public static Optional<NetNannyProfile> setCategories(NetNannyProfile profile, CredentialService credentialService, String menuItem, List<AbstractFilterCategory> filterCategories) {
        Optional<NetNannyFiltersPage> filtersOpt = profile.clickFiltersMenu(menuItem);
        log.info("Opening restrictions menu");
        if (!filtersOpt.isPresent()) {
            log.error("Restrictions menu not present");
            return Optional.empty();
        }

        NetNannyFiltersPage filtersPage = filtersOpt.get();
        filtersPage.findAndDo(credentialService, filterCategories, true);
        return Optional.of(filtersPage.close());
    }
}
