package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.jobs.pages.NetNannyDashboard;
import com.hyperion.selfcontrol.jobs.pages.NetNannyLoginPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class NetNannyBaseJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyBaseJob.class);

    public static Optional<NetNannyProfile> navigateToProfile(WebDriver driver, CredentialService credentialService) {
        driver.manage().deleteAllCookies();
        driver.get("chrome://settings/clearBrowserData");
        WebElement settings = driver.findElement(By.xpath("//settings-ui"));
        settings.sendKeys(Keys.ENTER);

        log.info("Driver constructed");
        NetNannyLoginPage loginPage = new NetNannyLoginPage(driver, credentialService);

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
}
