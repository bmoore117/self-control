package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.jobs.pages.NetNannyDashboard;
import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyLoginPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

public class NetNannySetCategoryJob {

    private static final Logger log = LoggerFactory.getLogger(NetNannyStatusJob.class);

    public static boolean setCategory(CredentialService credentialService, String category, boolean allowed) {
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
                return false;
            }

            log.info("Doing login");
            Optional<NetNannyDashboard> dashboardOpt = loginPage.doLogin();
            if (!dashboardOpt.isPresent()) {
                log.error("Dashboard not present");
                return false;
            }

            NetNannyDashboard dashboard = dashboardOpt.get();
            log.info("Clicking profile");
            Optional<NetNannyProfile> profileOpt = dashboard.clickProfile();
            if (!profileOpt.isPresent()) {
                log.error("Profile not present");
                return false;
            }

            NetNannyProfile profile = profileOpt.get();
            log.info("Opening restrictions menu");
            Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu();
            if (!filtersOpt.isPresent()) {
                log.error("Restrictions menu not present");
                return false;
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
                Method m = methodOpt.get();
                try {
                    m.invoke(filtersPage);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Error invoking method, " + m.getName(), e);
                    return false;
                }
            }

            return true;
        } catch (MalformedURLException e) {
            log.error("Malformed selenium host url", e);
            return false;
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
    }
}
