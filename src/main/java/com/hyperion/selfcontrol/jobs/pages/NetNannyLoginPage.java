package com.hyperion.selfcontrol.jobs.pages;

import com.hyperion.selfcontrol.backend.CredentialService;
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
    private CredentialService credentialService;

    public NetNannyLoginPage(WebDriver webDriver, CredentialService credentialService) {
        this.driver = webDriver;
        this.credentialService = credentialService;
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

        email.sendKeys(credentialService.getNetNannyUsername());
        password.sendKeys(credentialService.getNetNannyPassword());
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
