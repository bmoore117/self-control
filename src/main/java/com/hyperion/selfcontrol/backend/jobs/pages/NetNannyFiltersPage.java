package com.hyperion.selfcontrol.backend.jobs.pages;

import com.hyperion.selfcontrol.backend.*;
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

    private final WebDriver driver;
    private final WebElement modal;

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

    public void findAndDo(ConfigService configService, List<AbstractFilterCategory> filterCategories, boolean writeFile) {
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
                        updateContentFilters(configService.getConfig().getState().getCustomContentFilters(), filterCategory.getName(), filterCategory.getStatus());
                    } else {
                        updateContentFilters(configService.getConfig().getState().getContentFilters(), filterCategory.getName(), filterCategory.getStatus());
                    }
                    configService.writeFile();
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
        List<String> filterNames = elements.stream().map(e -> {
            if (!e.isDisplayed()) {
                scrollIntoView(e, driver);
            }
            WebElement listLink = e.findElement(By.cssSelector("div.filter-name.one-liner"));
            return listLink.getText();
        }).collect(Collectors.toList());

        List<CustomFilterCategory> results = new ArrayList<>();
        for (String filterName : filterNames) {
            wait = new WebDriverWait(driver, 10);
            List<WebElement> filters = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.filters-list")));
            WebElement targetFilter = null;
            for (WebElement filter : filters) {
                if (!filter.isDisplayed()) {
                    scrollIntoView(filter, driver);
                }
                if (filter.getText().contains(filterName)) {
                    targetFilter = filter;
                    break;
                }
            }

            if (targetFilter != null) {
                results.add(processElement(targetFilter));
            } else {
                log.error("Could not find element for {}", filterName);
            }
        }

        if (results.isEmpty()) {
            log.error("No custom filters found in modal, returning empty list");
        }

        return results;
    }

    private CustomFilterCategory processElement(WebElement e) {
        String[] textParts = e.getText().split("\\W+");
        StringBuilder category = new StringBuilder();
        int length = textParts.length - 3;
        for (int i = 0; i < length; i++) {
            category.append(textParts[i]).append(" ");
        }

        WebElement activeButton = e.findElement(By.cssSelector("button.active"));
        String status = activeButton.getText();

        WebElement listLink = e.findElement(By.cssSelector("div.filter-name.one-liner"));
        listLink.click();

        List<WebElement> keywords = driver.findElements(By.cssSelector("li.keyword"));
        List<Keyword> collectedKeywords = keywords.stream().map(li -> {
            if (!li.isDisplayed()) {
                scrollIntoView(li, driver);
            }
            return new Keyword(li.getText());
        }).collect(Collectors.toList());

        WebElement backDiv = driver.findElement(By.cssSelector("div.back"));
        backDiv.click();

        CustomFilterCategory f = new CustomFilterCategory(category.toString().trim(), status, collectedKeywords);
        log.info(f.toString());
        return f;
    }

    public boolean upsertCustomFilterCategory(CustomFilterCategory category) {
        try {
            log.info("Searching for existing category with name: {}", category.getName());
            List<WebElement> filterItems = driver.findElements(By.cssSelector("div.filter-item"));
            WebElement existingCategory = null;
            for (WebElement element : filterItems) {
                if (!element.isDisplayed()) {
                    scrollIntoView(element, driver);
                }
                if (element.getText().contains(category.getName())) {
                    log.info("Existing category found");
                    existingCategory = element.findElement(By.cssSelector("div.filter-name"));
                    break;
                }
            }

            if (existingCategory != null) {
                existingCategory.click();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            } else {
                log.info("Existing category not found, creating new category");
                WebElement addNew = driver.findElement(By.cssSelector("div.add-new"));
                addNew.click();

                WebElement nameField = driver.findElement(By.cssSelector("input.filter-name-input"));
                nameField.sendKeys(category.getName());
            }

            WebElement modal = driver.findElement(By.cssSelector("div.modal"));
            WebElement addButton = modal.findElement(By.cssSelector("svg.btn-plus"));
            if (addButton.isDisplayed()) {
                scrollIntoView(addButton, driver);
            }
            addButton.click();

            boolean madeRemoval;
            do {
                madeRemoval = false;
                WebElement phraseList = driver.findElement(By.cssSelector("ul.filter-keywords-list"));
                List<WebElement> phrases = phraseList.findElements(By.tagName("li"));
                for (WebElement phrase : phrases) {
                    if (!phrase.isDisplayed()) {
                        scrollIntoView(phrase, driver);
                    }
                    if (!category.getKeywords().contains(new Keyword(phrase.getText().trim()))) {
                        log.info("Removing element {}", phrase.getText().trim());
                        WebElement removeButton = phrase.findElement(By.tagName("svg"));
                        removeButton.click();
                        madeRemoval = true;
                        break;
                    }
                }
            } while (madeRemoval);

            for (Keyword keyword : category.getKeywords()) {
                log.info("Adding phrase {}", keyword.getValue());
                WebElement phraseField = driver.findElement(By.cssSelector("input.text-field"));
                if (!phraseField.isDisplayed()) {
                    scrollIntoView(phraseField, driver);
                }
                phraseField.sendKeys(keyword.getValue());
                WebElement addPhraseButton = driver.findElement(By.cssSelector("button.base-button.btn-add"));
                if (!addPhraseButton.isDisplayed()) {
                    scrollIntoView(addPhraseButton, driver);
                }
                addPhraseButton.click();
            }

            WebElement finalSubmitButton;
            if (existingCategory != null) {
                finalSubmitButton = driver.findElement(By.cssSelector("button.base-button.update-button"));
            } else {
                finalSubmitButton = driver.findElement(By.cssSelector("button.base-button.create-button"));
            }
            if (!finalSubmitButton.isDisplayed()) {
                scrollIntoView(finalSubmitButton, driver);
            }
            finalSubmitButton.click();

            log.info("Job successful");
            return true;
        } catch (RuntimeException ex) {
            log.error("Error upserting", ex);
            return false;
        }
    }

    public boolean deleteCustomFilterCategory(CustomFilterCategory category) {
        try {
            List<WebElement> filterItems = modal.findElements(By.cssSelector("div.filter-item"));
            for (WebElement element : filterItems) {
                if (!element.isDisplayed()) {
                    scrollIntoView(element, driver);
                }
                if (element.getText().contains(category.getName())) {
                    WebElement link = element.findElement(By.cssSelector("div.filter-name"));
                    link.click();

                    WebElement deleteButton = driver.findElement(By.cssSelector("button.base-button.delete-button"));
                    deleteButton.click();

                    return true;
                }
            }

            return false;
        } catch (RuntimeException ex) {
            log.error("Error deleting", ex);
            return false;
        }
    }
}
