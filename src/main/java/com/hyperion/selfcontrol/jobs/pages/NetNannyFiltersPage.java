package com.hyperion.selfcontrol.jobs.pages;

import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import com.hyperion.selfcontrol.backend.FilterCategory;
import com.hyperion.selfcontrol.jobs.NetNannyBaseJob;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NetNannyFiltersPage {

    private static final Logger log = LoggerFactory.getLogger(NetNannyFiltersPage.class);

    private WebDriver driver;
    private WebElement modal;

    public NetNannyFiltersPage(WebDriver driver, WebElement modal) {
        this.driver = driver;
        this.modal = modal;
    }

    public NetNannyProfile close() {
        WebElement webElement = driver.findElement(By.cssSelector("svg.btn-close"));
        log.info("Clicking close button for content filters page");
        webElement.click();
        return new NetNannyProfile(driver);
    }

    public void findAndDo(String category, String action) {
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
                        NetNannyBaseJob.scrollIntoView(e, driver);
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

    public List<FilterCategory> getStatuses() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        Boolean li = wait.until(driver -> !modal.findElements(By.tagName("li")).isEmpty());
        List<WebElement> lis = new ArrayList<>();
        if (li != null && li) {
            lis.addAll(modal.findElements(By.tagName("li")));
        }

        List<FilterCategory> collect = lis.stream()
                .map(e -> {
                    if (!e.isDisplayed()) {
                        NetNannyBaseJob.scrollIntoView(e, driver);
                    }
                    String[] textParts = e.getText().split("\\W+");
                    StringBuilder category = new StringBuilder();
                    int length = textParts.length - 3;
                    for (int i = 0; i < length; i++) {
                        category.append(textParts[i]).append(" ");
                    }

                    WebElement activeButton = e.findElement(By.cssSelector("button.active"));
                    String status = activeButton.getText();
                    FilterCategory f = new FilterCategory(category.toString().trim(), status);
                    log.info(f.toString());
                    return f;
                }).collect(Collectors.toList());

        if (collect.isEmpty()) {
            log.error("No lis found in modal, returning empty list");
        }

        return collect;
    }

    public List<CustomFilterCategory> getCustomStatuses() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.filters-list")));

        List<CustomFilterCategory> collect = elements.stream()
                .map(e -> {
                    if (!e.isDisplayed()) {
                        NetNannyBaseJob.scrollIntoView(e, driver);
                    }
                    String[] textParts = e.getText().split("\\W+");
                    StringBuilder category = new StringBuilder();
                    int length = textParts.length - 3;
                    for (int i = 0; i < length; i++) {
                        category.append(textParts[i]).append(" ");
                    }

                    WebElement activeButton = e.findElement(By.cssSelector("button.active"));
                    String status = activeButton.getText();
                    CustomFilterCategory f = new CustomFilterCategory(category.toString().trim(), status);
                    log.info(f.toString());
                    return f;
                }).collect(Collectors.toList());

        if (collect.isEmpty()) {
            log.error("No custom filters found in modal, returning empty list");
        }

        return collect;
    }
}
