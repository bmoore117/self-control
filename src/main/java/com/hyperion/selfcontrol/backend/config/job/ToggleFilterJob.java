package com.hyperion.selfcontrol.backend.config.job;

import com.hyperion.selfcontrol.backend.AbstractFilterCategory;

import java.time.LocalDateTime;
import java.util.List;

public class ToggleFilterJob extends Job {

    private String menuItem;
    private List<AbstractFilterCategory> filterCategories;

    public ToggleFilterJob() {}

    public ToggleFilterJob(LocalDateTime jobLaunchTime, String jobDescription, String menuItem, List<AbstractFilterCategory> filterCategories) {
        super(jobLaunchTime, jobDescription);
        this.menuItem = menuItem;
        this.filterCategories = filterCategories;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    public List<AbstractFilterCategory> getFilterCategories() {
        return filterCategories;
    }

    public void setFilterCategories(List<AbstractFilterCategory> filterCategories) {
        this.filterCategories = filterCategories;
    }
}
