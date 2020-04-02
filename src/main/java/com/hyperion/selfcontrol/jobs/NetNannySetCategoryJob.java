package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class NetNannySetCategoryJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    public static Optional<NetNannyProfile> setCategory(NetNannyProfile profile, CredentialService credentialService, String menuItem, String category, String action) {
        Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu(menuItem);
        log.info("Opening restrictions menu");
        if (!filtersOpt.isPresent()) {
            log.error("Restrictions menu not present");
            return Optional.empty();
        }

        NetNannyFiltersPage filtersPage = filtersOpt.get();
        filtersPage.findAndDo(credentialService, category.toLowerCase(), action);
        return Optional.of(filtersPage.close());
    }
}
