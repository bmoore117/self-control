package com.hyperion.selfcontrol.jobs.pages;

import com.hyperion.selfcontrol.jobs.NetNannyBaseJob;
import org.openqa.selenium.By;
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

    public Optional<NetNannyFiltersPage> clickMenu(String menuItem) {
        menu.click();

        long start = System.currentTimeMillis();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> restrictions = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div.restrictions-item")));
        log.info("Elapsed wait time for restrictions menu: " + (System.currentTimeMillis() - start));

        if (restrictions.isEmpty()) {
            log.error("Restrictions menu not found");
        }

        Optional<WebElement> filters = restrictions.stream()
                .filter(e -> menuItem.equals(e.getText().toLowerCase())).findFirst();

        if (filters.isPresent()) {
            filters.get().click();
        } else {
            log.error("content filters not found");
            return Optional.empty();
        }

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
}
