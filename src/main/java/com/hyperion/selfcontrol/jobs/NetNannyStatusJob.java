package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.FilterCategory;
import com.hyperion.selfcontrol.jobs.pages.NetNannyDashboard;
import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyLoginPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NetNannyStatusJob {

    public static List<FilterCategory> getNetNannyStatuses(CredentialService credentialService) {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(
                    new URL("http://0.0.0.0:4444/wd/hub"),
                    capabilities);

            System.out.println("Driver constructed");
            NetNannyLoginPage loginPage = new NetNannyLoginPage(driver, credentialService);

            if (!loginPage.navigateToLoginPage()) {
                System.out.println("Login page not present");
                return Collections.emptyList();
            }

            System.out.println("Doing login");
            Optional<NetNannyDashboard> dashboardOpt = loginPage.doLogin();
            if (!dashboardOpt.isPresent()) {
                System.out.println("Dashboard not present");
                return Collections.emptyList();
            }

            NetNannyDashboard dashboard = dashboardOpt.get();
            System.out.println("Clicking profile");
            Optional<NetNannyProfile> profileOpt = dashboard.clickProfile();
            if (!profileOpt.isPresent()) {
                System.out.println("Profile not present");
                return Collections.emptyList();
            }

            NetNannyProfile profile = profileOpt.get();
            Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu();
            System.out.println("Opening restrictions menu");
            if (!filtersOpt.isPresent()) {
                System.out.println("Restrictions menu not present");
                return Collections.emptyList();
            }

            NetNannyFiltersPage filtersPage = filtersOpt.get();
            List<FilterCategory> filterCategories = filtersPage.getStatuses();

            return filterCategories;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
    }
}
