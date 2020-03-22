package com.hyperion.selfcontrol.jobs;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.jobs.pages.NetNannyDashboard;
import com.hyperion.selfcontrol.jobs.pages.NetNannyFiltersPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyLoginPage;
import com.hyperion.selfcontrol.jobs.pages.NetNannyProfile;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

public class NetNannySetCategoryJob {

    public static boolean setCategory(CredentialService credentialService, String category, boolean allowed) {
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
                return false;
            }

            System.out.println("Doing login");
            Optional<NetNannyDashboard> dashboardOpt = loginPage.doLogin();
            if (!dashboardOpt.isPresent()) {
                System.out.println("Dashboard not present");
                return false;
            }

            NetNannyDashboard dashboard = dashboardOpt.get();
            System.out.println("Clicking profile");
            Optional<NetNannyProfile> profileOpt = dashboard.clickProfile();
            if (!profileOpt.isPresent()) {
                System.out.println("Profile not present");
                return false;
            }

            NetNannyProfile profile = profileOpt.get();
            System.out.println("Opening restrictions menu");
            Optional<NetNannyFiltersPage> filtersOpt = profile.clickMenu();
            if (!filtersOpt.isPresent()) {
                System.out.println("Restrictions menu not present");
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
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
    }
}
