package com.hyperion.selfcontrol.jobs.pages;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.jobs.NetNannyBaseJob;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class NetNannyProfile {

    private static final Logger log = LoggerFactory.getLogger(NetNannyProfile.class);

    private WebDriver driver;
    private WebElement menu;

    private List<WebElement> restrictions;

    public NetNannyProfile(WebDriver driver, WebElement menu) {
        this.driver = driver;
        this.menu = menu;
        menu.click();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        restrictions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.restrictions-item")));
    }

    public NetNannyProfile(WebDriver driver) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, 10);
        this.menu = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.button-standard-restrictions.standard-restrictions-menu")));
        menu.click();
        restrictions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.restrictions-item")));
    }

    public Optional<NetNannyFiltersPage> clickMenu(String menuItem) {
        if (restrictions.isEmpty()) {
            log.error("Restrictions items not found");
            return Optional.empty();
        }

        Optional<WebElement> filters = restrictions.stream()
                .filter(e -> menuItem.equals(e.getText().toLowerCase())).findFirst();

        if (filters.isPresent()) {
            filters.get().click();
        } else {
            log.error("content filters not found");
            return Optional.empty();
        }

        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.modal")));

        if (modal != null) {
            if (!modal.isDisplayed()) {
                log.info("Scrolling modal into view");
                NetNannyBaseJob.scrollIntoView(modal, driver);
            }
            Optional<NetNannyFiltersPage> filtersPage = Optional.of(new NetNannyFiltersPage(driver, modal));
            log.info("Returning optional: " + filtersPage);
            return filtersPage;
        } else {
            return Optional.empty();
        }
    }

    public boolean isForceSafeSearch() {
        if (restrictions.isEmpty()) {
            log.error("Restrictions items not found");
        }

        Optional<WebElement> safeSearch = restrictions.stream()
                .filter(e -> "force safe search".equals(e.getText().toLowerCase())).findFirst();

        if (safeSearch.isPresent()) {
            WebElement checkBox = safeSearch.get().findElement(By.cssSelector("input[type=checkbox]"));
            return checkBox.isSelected();
        } else {
            log.error("safe search item not found");
            return false;
        }
    }

    public void setForceSafeSearch(boolean enabled, CredentialService credentialService) {
        if (restrictions.isEmpty()) {
            log.error("Restrictions items not found");
        }

        Optional<WebElement> safeSearch = restrictions.stream()
                .filter(e -> "force safe search".equals(e.getText().toLowerCase())).findFirst();

        if (safeSearch.isPresent()) {
            WebElement checkBox = safeSearch.get().findElement(By.cssSelector("input[type=checkbox]"));
            if (!checkBox.isSelected() && enabled) {
                log.info("Clicking checkbox on");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkBox);
                credentialService.getConfig().getState().setForceSafeSearch(true);
                credentialService.writeFile();
            } else if (checkBox.isSelected() && !enabled) {
                log.info("Clicking checkbox off");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkBox);
                credentialService.getConfig().getState().setForceSafeSearch(false);
                credentialService.writeFile();
            }
        } else {
            log.error("safe search item not found");
        }
    }
}
