package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class NetNannySetCategoryJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    public static Optional<NetNannyProfile> setCategory(NetNannyProfile profile, String category, boolean allowed) {
        Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu();
        log.info("Opening restrictions menu");
        if (!filtersOpt.isPresent()) {
            log.error("Restrictions menu not present");
            return Optional.empty();
        }

        NetNannyFiltersPage filtersPage = filtersOpt.get();
        Optional<Method> methodOpt = Arrays.stream(filtersPage.getClass().getDeclaredMethods())
                .filter(m -> {
                    String categoryNoSpace;
                    if (category.contains(" ")) {
                        categoryNoSpace = category.replace(" ", "");
                    } else {
                        categoryNoSpace = category;
                    }

                    if (allowed) {
                        return m.getName().equals("allow" + categoryNoSpace);
                    } else {
                        return m.getName().equals("block" + categoryNoSpace);
                    }
                }).findFirst();

        if (methodOpt.isPresent()) {
            log.info("Found method, invoking");
            Method m = methodOpt.get();
            try {
                m.invoke(filtersPage);
                return Optional.of(filtersPage.close());
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Error invoking method, " + m.getName(), e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
