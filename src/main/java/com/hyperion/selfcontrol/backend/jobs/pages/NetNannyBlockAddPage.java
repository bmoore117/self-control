package com.hyperion.selfcontrol.backend.jobs.pages;

import com.hyperion.selfcontrol.backend.Website;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob.scrollIntoView;

public class NetNannyBlockAddPage {

    private static final Logger log = LoggerFactory.getLogger(NetNannyBlockAddPage.class);

    private WebElement modal;
    private WebDriver webDriver;

    public NetNannyBlockAddPage(WebElement modal, WebDriver webDriver) {
        this.modal = modal;
        this.webDriver = webDriver;
    }

    public boolean addItem(String website, boolean isAllow) {
        String postFix;
        if (isAllow) {
            postFix = "allow";
        } else {
            postFix = "block";
        }

        return modal.findElements(By.cssSelector("ul.urls-list")).stream()
                .filter(element -> element.findElements(By.cssSelector("div.list-title." + postFix)).size() > 0)
                .findFirst()
                .map(urlList -> {
                    boolean alreadyExists = urlList.findElements(By.cssSelector("div.website-list-item")).stream()
                            .anyMatch(e -> e.getText().contains(website));
                    if (alreadyExists) {
                        log.info("Site {} already {}ed", website, postFix);
                        return true;
                    }

                    WebElement addButton = urlList.findElement(By.cssSelector("svg.btn-plus"));
                    if (!addButton.isDisplayed()) {
                        NetNannyBaseJob.scrollIntoView(addButton, webDriver);
                    }

                    addButton.click();
                    WebElement textField = urlList.findElement(By.cssSelector("input.text-field"));
                    textField.sendKeys(website);
                    WebElement inputDiv = urlList.findElement(By.cssSelector("div.website-settings-input"));
                    WebElement button = inputDiv.findElement(By.tagName("button"));
                    button.click();

                    if (!isAllow) {
                        WebElement confirmBtn = urlList.findElement(By.cssSelector("div.btn.btn-confirm"));
                        confirmBtn.click();
                    }

                    return true;
                }).orElse(false);
    }

    public boolean removeItem(String website, boolean isAllow) {
        String postFix;
        if (isAllow) {
            postFix = "allow";
        } else {
            postFix = "block";
        }

        return modal.findElements(By.cssSelector("ul.urls-list")).stream()
                .filter(element -> element.findElements(By.cssSelector("div.list-title." + postFix)).size() > 0)
                .findFirst()
                .flatMap(urlList -> urlList.findElements(By.tagName("li"))
                        .stream().filter(element -> {
                            if (!element.isDisplayed()) {
                                scrollIntoView(element, webDriver);
                            }
                            return element.getText().equals(website);
                        }).findFirst()
                )
                .flatMap(item -> {
                    WebElement button = item.findElement(By.cssSelector("svg.btn-close"));
                    button.click();
                    return modal.findElements(By.cssSelector("ul.urls-list")).stream()
                            .filter(element -> element.findElements(By.cssSelector("div.list-title." + postFix)).size() > 0)
                            .findFirst()
                            .map(urlList -> urlList.findElements(By.tagName("li")).stream().allMatch(element -> {
                                try {
                                    if (!element.isDisplayed()) {
                                        scrollIntoView(element, webDriver);
                                    }
                                    return !element.getText().equals(website);
                                } catch (StaleElementReferenceException e) {
                                    // true that there's no match
                                    return true;
                                }
                            }));
                }).orElse(false);

    }

    public List<Website> getItems(boolean allowedItems) {
        String postFix;
        if (allowedItems) {
            postFix = "allow";
        } else {
            postFix = "block";
        }

        return modal.findElements(By.cssSelector("ul.urls-list")).stream()
                .filter(element -> element.findElements(By.cssSelector("div.list-title." + postFix)).size() > 0)
                .findFirst()
                .map(urlList -> urlList.findElements(By.tagName("li")).stream().map(e -> {
                    if (!e.isDisplayed()) {
                        scrollIntoView(e, webDriver);
                    }
                    return new Website(e.getText());
                }).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
