package com.hyperion.selfcontrol.jobs.pages;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NetNannyCustomFiltersPage {

    private static final Logger log = LoggerFactory.getLogger(NetNannyCustomFiltersPage.class);

    private WebDriver driver;
    private WebElement modal;

    public NetNannyCustomFiltersPage(WebDriver driver, WebElement modal) {
        this.driver = driver;
        this.modal = modal;
    }

    public NetNannyProfile close() {
        WebElement webElement = driver.findElement(By.cssSelector("svg.btn-close"));
        log.info("Clicking close button for content filters page");
        webElement.click();
        return new NetNannyProfile(driver);
    }

    private void findAndDo(String category, String action) {
        Optional<WebElement> li = modal.findElements(By.tagName("li")).stream()
                .filter(e -> e.getText().toLowerCase().contains(category))
                .findFirst();

        if (li.isPresent()) {
            Optional<WebElement> allow = li.get().findElements(By.tagName("button")).stream()
                    .filter(b -> b.getText().toLowerCase().contains(action))
                    .findFirst();

            allow.ifPresent(WebElement::click);
        }
    }

    private Optional<String> find(String category) {
        Optional<WebElement> li = modal.findElements(By.tagName("li")).stream()
                .filter(e -> {
                    if (!e.isDisplayed()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            log.error("Thread interrupted while sleeping after scroll", ex);
                        }
                    }
                    return e.getText().toLowerCase().contains(category);
                }).findFirst();

        if (li.isPresent()) {
            Optional<WebElement> allow = li.get().findElements(By.cssSelector("button.active")).stream()
                    .findFirst();

            return allow.map(WebElement::getText);
        }

        return Optional.empty();
    }

    public List<CustomFilterCategory> getStatuses() {
        return modal.findElements(By.tagName("li")).stream()
                .map(e -> {
                    if (!e.isDisplayed()) {
                        log.info("Scrolling element into view");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            log.error("Thread interrupted while sleeping after scroll", ex);
                        }
                    }
                    String[] textParts = e.getText().split("\\W+");

                    String category = "";
                    int length = textParts.length - 3;
                    for (int i = 0; i < length; i++) {
                        category += textParts[i];
                    }

                    WebElement activeButton = e.findElement(By.cssSelector("button.active"));
                    String status = activeButton.getText();
                    CustomFilterCategory f = new CustomFilterCategory(category, status);
                    log.info(f.toString());
                    return f;
                }).collect(Collectors.toList());
    }
}
