package com.hyperion.selfcontrol.jobs.pages;

import com.hyperion.selfcontrol.backend.FilterCategory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<FilterCategory> getStatuses() {
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
                    String category;
                    if (textParts.length == 4) {
                        category = textParts[0];
                    } else {
                        category = textParts[0] + " " + textParts[1];
                    }

                    WebElement activeButton = e.findElement(By.cssSelector("button.active"));
                    String status = activeButton.getText();
                    FilterCategory f = new FilterCategory(category, status);
                    log.info(f.toString());
                    return f;
                }).collect(Collectors.toList());
    }

    public String getPornStatus() {
        return find("porn").orElse("error");
    }

    public String getMatureContentStatus() {
        return find("mature content").orElse("error");
    }

    public String getAdultNoveltyStatus() {
        return find("adult novelty").orElse("error");
    }

    public String getAnimeStatus() {
        return find("anime").orElse("error");
    }

    public String getNudityStatus() {
        return find("nudity").orElse("error");
    }

    public String getAbortionStatus() {
        return find("abortion").orElse("error");
    }

    public String getStripClubStatus() {
        return find("strip clubs").orElse("error");
    }

    public String getProvocativeStatus() {
        return find("provocative").orElse("error");
    }

    public String getDeathGoreStatus() {
        return find("porn").orElse("error");
    }

    public String getTobaccoStatus() {
        return find("tobacco").orElse("error");
    }

    public String getDrugsStatus() {
        return find("drugs").orElse("error");
    }

    public String getWeaponsStatus() {
        return find("weapons").orElse("error");
    }

    public String getGamblingStatus() {
        return find("gambling").orElse("error");
    }

    public String getSuicideStatus() {
        return find("suicide").orElse("error");
    }

    public void allowPorn() {
        findAndDo("porn", "allow");
    }

    public void blockPorn() {
        findAndDo("porn", "block");
    }

    public void allowMatureContent() {
        findAndDo("mature content", "allow");
    }

    public void blockMatureContent() {
        findAndDo("mature content", "block");
    }

    public void allowAdultNovelty() {
        findAndDo("adult novelty", "allow");
    }

    public void blockAdultNovelty() {
        findAndDo("adult novelty", "block");
    }

    public void allowAnime() {
        findAndDo("anime", "allow");
    }

    public void blockAnime() {
        findAndDo("anime", "block");
    }

    public void allowNudity() {
        findAndDo("nudity", "allow");
    }

    public void blockNudity() {
        findAndDo("nudity", "block");
    }

    public void allowAbortion() {
        findAndDo("abortion", "allow");
    }

    public void blockAbortion() {
        findAndDo("abortion", "block");
    }

    public void allowStripClubs() {
        findAndDo("strip clubs", "allow");
    }

    public void blockStripClubs() {
        findAndDo("strip clubs", "block");
    }

    public void allowProvocative() {
        findAndDo("provocative", "allow");
    }

    public void blockProvocative() {
        findAndDo("provocative", "block");
    }

    public void allowDeathGore() {
        findAndDo("death gore", "allow");
    }

    public void blockDeathGore() {
        findAndDo("death gore", "block");
    }

    public void allowTobacco() {
        findAndDo("tobacco", "allow");
    }

    public void blockTobacco() {
        findAndDo("tobacco", "block");
    }

    public void allowDrugs() {
        findAndDo("drugs", "allow");
    }

    public void blockDrugs() {
        findAndDo("drugs", "block");
    }

    public void allowWeapons() {
        findAndDo("weapons", "allow");
    }

    public void blockWeapons() {
        findAndDo("weapons", "block");
    }

    public void allowGambling() {
        findAndDo("gambling", "allow");
    }

    public void blockGambling() {
        findAndDo("gambling", "block");
    }

    public void allowSuicide() {
        findAndDo("suicide", "allow");
    }

    public void blockSuicide() {
        findAndDo("suicide", "block");
    }
}
