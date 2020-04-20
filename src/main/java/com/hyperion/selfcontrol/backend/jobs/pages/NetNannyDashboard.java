package com.hyperion.selfcontrol.backend.jobs.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Optional;

public class NetNannyDashboard {

    private WebDriver driver;

    public NetNannyDashboard(WebDriver webDriver) {
        this.driver = webDriver;
    }

    public Optional<NetNannyProfile> clickProfile() {
        WebElement profile = driver.findElement(By.cssSelector("div.name.one-liner"));
        profile.click();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.button-standard-restrictions.standard-restrictions-menu")));

        if (menu == null) {
            return Optional.empty();
        } else {
            return Optional.of(new NetNannyProfile(driver, menu));
        }
    }


}
