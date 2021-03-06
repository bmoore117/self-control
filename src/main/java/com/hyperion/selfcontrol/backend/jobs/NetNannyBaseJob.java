package com.hyperion.selfcontrol.backend.jobs;

import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyDashboard;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyLoginPage;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyProfile;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class NetNannyBaseJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyBaseJob.class);

    public static Optional<NetNannyProfile> navigateToProfile(WebDriver driver, ConfigService configService) {
        driver.manage().deleteAllCookies();
        driver.get("chrome://settings/clearBrowserData");
        WebElement settings = driver.findElement(By.xpath("//settings-ui"));
        settings.sendKeys(Keys.ENTER);

        log.info("Driver constructed");
        NetNannyLoginPage loginPage = new NetNannyLoginPage(driver, configService);

        if (!loginPage.navigateToLoginPage()) {
            log.error("Login page not present");
            return Optional.empty();
        }

        log.info("Doing login");
        Optional<NetNannyDashboard> dashboardOpt = loginPage.doLogin();
        if (!dashboardOpt.isPresent()) {
            log.error("Dashboard not present");
            return Optional.empty();
        }

        NetNannyDashboard dashboard = dashboardOpt.get();
        log.info("Clicking profile");
        Optional<NetNannyProfile> profileOpt = dashboard.clickProfile();
        if (!profileOpt.isPresent()) {
            log.error("Profile not present");
            return Optional.empty();
        }

        return profileOpt;
    }

    public static void scrollIntoView(WebElement element, WebDriver driver) {
        log.info("Scrolling element into view");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            log.error("Thread interrupted while sleeping after scroll", ex);
        }
    }
}
