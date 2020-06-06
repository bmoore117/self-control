package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.AbstractFilterCategory;

import java.time.LocalDateTime;
import java.util.List;

public class ToggleFilterJob extends OnlineJob {

    private static final String MENU_ITEM = "menuItem";
    private static final String FILTER_CATEGORIES = "filterCategories";
    private List<AbstractFilterCategory> filterCategories;

    public ToggleFilterJob() {}

    public ToggleFilterJob(LocalDateTime jobLaunchTime, String jobDescription, String menuItem, List<AbstractFilterCategory> filterCategories) {
        super(jobLaunchTime, jobDescription);
        data.put(MENU_ITEM, menuItem);
        data.put(FILTER_CATEGORIES, filterCategories);
    }

    public String getMenuItem() {
        return get(MENU_ITEM, String.class);
    }

    public void setMenuItem(String menuItem) {
        data.put(MENU_ITEM, menuItem);
    }

    public List<AbstractFilterCategory> getFilterCategories() {
        return get(FILTER_CATEGORIES, List.class);
    }

    public void setFilterCategories(List<AbstractFilterCategory> filterCategories) {
        data.put(FILTER_CATEGORIES, filterCategories);
    }
}
