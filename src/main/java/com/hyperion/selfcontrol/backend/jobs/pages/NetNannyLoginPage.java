package com.hyperion.selfcontrol.backend.jobs.pages;

import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.Credentials;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class NetNannyLoginPage {

    private static final Logger log = LoggerFactory.getLogger(NetNannyLoginPage.class);

    private WebDriver driver;
    private ConfigService configService;

    public NetNannyLoginPage(WebDriver webDriver, ConfigService configService) {
        this.driver = webDriver;
        this.configService = configService;
    }

    public boolean navigateToLoginPage() {
        driver.navigate().to("https://parent.netnanny.com/#/login");
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("email")));

        return email != null;
    }

    public Optional<NetNannyDashboard> doLogin() {
        WebElement email = driver.findElement(By.id("email"));
        WebElement password = driver.findElement(By.id("password"));

        Optional<Credentials> credOpt = configService.getNetNanny();
        if (!credOpt.isPresent()) {
            log.error("Missing net nanny credentials, check file");
            return Optional.empty();
        }
        Credentials netNanny = credOpt.get();
        email.sendKeys(netNanny.getUsername());
        password.sendKeys(netNanny.getPassword());
        password.sendKeys(Keys.ENTER);

        boolean contains = driver.getPageSource().toLowerCase().contains("your screentime parenting ally");
        int tries = 0;
        while (!contains && tries < 3) {
            try {
                tries++;
                Thread.sleep(1000);
                contains = driver.getPageSource().toLowerCase().contains("your screentime parenting ally");
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for main dashboard", e);
            }
        }

        if (!contains) {
            return Optional.empty();
        } else {
            return Optional.of(new NetNannyDashboard(driver));
        }
    }
}
