package com.hyperion.selfcontrol.backend.jobs.pages;

import com.hyperion.selfcontrol.backend.AbstractFilterCategory;
import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import com.hyperion.selfcontrol.backend.FilterCategory;
import com.hyperion.selfcontrol.backend.config.ContentFilter;
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

import static com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob.scrollIntoView;

public class NetNannyFiltersPage {

    private static final Logger log = LoggerFactory.getLogger(NetNannyFiltersPage.class);

    private WebDriver driver;
    private WebElement modal;

    public static final String CONTENT_FILTERS = "net nanny content filters";
    public static final String CUSTOM_CONTENT_FILTERS = "custom content filters";

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

    public void findAndDo(CredentialService credentialService, List<AbstractFilterCategory> filterCategories, boolean writeFile) {
        for (AbstractFilterCategory filterCategory : filterCategories) {
            Optional<WebElement> row;
            boolean isCustomContentFilter;
            List<WebElement> rows;
            if (modal.getText().toLowerCase().contains(CUSTOM_CONTENT_FILTERS)) {
                isCustomContentFilter = true;
                rows = modal.findElements(By.cssSelector("div.filter-item"));
            } else {
                isCustomContentFilter = false;
                rows = modal.findElements(By.tagName("li"));
            }
            row = rows.stream()
                    .filter(e -> {
                        if (!e.isDisplayed()) {
                            scrollIntoView(e, driver);
                        }
                        return e.getText().toLowerCase().contains(filterCategory.getName().toLowerCase());
                    }).findFirst();

            if (row.isPresent()) {
                Optional<WebElement> button = row.get().findElements(By.tagName("button")).stream()
                        .filter(b -> b.getText().toLowerCase().contains(filterCategory.getStatus().toLowerCase()))
                        .findFirst();

                button.ifPresent(WebElement::click);

                if (writeFile) {
                    if (isCustomContentFilter) {
                        updateContentFilters(credentialService.getConfig().getState().getCustomContentFilters(), filterCategory.getName(), filterCategory.getStatus());
                    } else {
                        updateContentFilters(credentialService.getConfig().getState().getContentFilters(), filterCategory.getName(), filterCategory.getStatus());
                    }
                    credentialService.writeFile();
                }
            } else {
                log.warn("Expected row not present: " + filterCategory.getName());
            }
        }
    }

    private void updateContentFilters(List<ContentFilter> contentFilters, String category, String action) {
        Optional<ContentFilter> target = contentFilters.stream()
                .filter(c -> c.getName().equals(category))
                .findFirst();

        if (target.isPresent()) {
            target.get().setStatus(action);
        } else {
            contentFilters.add(new ContentFilter(category, action));
        }
    }

    private Optional<String> find(String category) {
        Optional<WebElement> li = modal.findElements(By.tagName("li")).stream()
                .filter(e -> {
                    if (!e.isDisplayed()) {
                        scrollIntoView(e, driver);
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
                        scrollIntoView(e, driver);
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
                        scrollIntoView(e, driver);
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
