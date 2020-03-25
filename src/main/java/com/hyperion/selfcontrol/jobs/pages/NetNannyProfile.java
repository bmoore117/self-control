package com.hyperion.selfcontrol.jobs.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Optional;

public class NetNannyProfile {

    private WebDriver driver;
    private WebElement menu;

    public NetNannyProfile(WebDriver driver, WebElement menu) {
        this.driver = driver;
        this.menu = menu;
    }

    public NetNannyProfile(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, 10);
        this.menu = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.button-standard-restrictions.standard-restrictions-menu")));
    }

    public Optional<NetNannyFiltersPage> clickMenu() {
        menu.click();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> restrictions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div.restrictions-item")));

        Optional<WebElement> filters = restrictions.stream()
                .filter(e -> "net nanny content filters".equals(e.getText().toLowerCase())).findFirst();

        if (filters.isPresent()) {
            filters.get().click();
        } else {
            return Optional.empty();
        }

        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.modal")));

        if (modal != null && modal.isDisplayed()) {
            return Optional.of(new NetNannyFiltersPage(driver, modal));
        } else {
            return Optional.empty();
        }
    }

    public Optional<NetNannyFiltersPage> clickCustomFiltersMenu() {
        menu.click();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> restrictions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div.restrictions-item")));

        Optional<WebElement> filters = restrictions.stream()
                .filter(e -> "custom content filters".equals(e.getText().toLowerCase())).findFirst();

        if (filters.isPresent()) {
            filters.get().click();
        } else {
            return Optional.empty();
        }

        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.modal")));

        if (modal != null && modal.isDisplayed()) {
            return Optional.of(new NetNannyFiltersPage(driver, modal));
        } else {
            return Optional.empty();
        }
    }
}
