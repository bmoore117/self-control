package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.FilterCategory;
import com.hyperion.selfcontrol.jobs.pages.NetNannyDashboard;
import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyLoginPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NetNannyStatusJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    public static List<FilterCategory> getNetNannyStatuses(CredentialService credentialService) {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(
                    new URL("http://0.0.0.0:4444/wd/hub"),
                    capabilities);

            driver.manage().deleteAllCookies();
            driver.get("chrome://settings/clearBrowserData");
            WebElement settings = driver.findElement(By.xpath("//settings-ui"));
            settings.sendKeys(Keys.ENTER);

            log.info("Driver constructed");
            NetNannyLoginPage loginPage = new NetNannyLoginPage(driver, credentialService);

            if (!loginPage.navigateToLoginPage()) {
                log.error("Login page not present");
                return Collections.emptyList();
            }

            log.info("Doing login");
            Optional<NetNannyDashboard> dashboardOpt = loginPage.doLogin();
            if (!dashboardOpt.isPresent()) {
                log.error("Dashboard not present");
                return Collections.emptyList();
            }

            NetNannyDashboard dashboard = dashboardOpt.get();
            log.info("Clicking profile");
            Optional<NetNannyProfile> profileOpt = dashboard.clickProfile();
            if (!profileOpt.isPresent()) {
                log.error("Profile not present");
                return Collections.emptyList();
            }

            NetNannyProfile profile = profileOpt.get();
            Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu();
            log.info("Opening restrictions menu");
            if (!filtersOpt.isPresent()) {
                log.error("Restrictions menu not present");
                return Collections.emptyList();
            }

            NetNannyFiltersPage filtersPage = filtersOpt.get();
            List<FilterCategory> filterCategories = filtersPage.getStatuses();

            return filterCategories;
        } catch (MalformedURLException e) {
            log.error("Malformed selenium host url", e);
            return Collections.emptyList();
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
    }
}
