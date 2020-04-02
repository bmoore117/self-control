package com.hyperion.selfcontrol.backend.config;

import java.util.LinkedList;
import java.util.List;

public class State {

    private List<ContentFilter> contentFilters;
    private List<ContentFilter> customContentFilters;
    private boolean forceSafeSearch;

    public List<ContentFilter> getContentFilters() {
        if (contentFilters == null) {
            contentFilters = new LinkedList<>();
        }
        return contentFilters;
    }

    public void setContentFilters(List<ContentFilter> contentFilters) {
        this.contentFilters = contentFilters;
    }

    public List<ContentFilter> getCustomContentFilters() {
        if (customContentFilters == null) {
            customContentFilters = new LinkedList<>();
        }
        return customContentFilters;
    }

    public void setCustomContentFilters(List<ContentFilter> customContentFilters) {
        this.customContentFilters = customContentFilters;
    }

    public boolean isForceSafeSearch() {
        return forceSafeSearch;
    }

    public void setForceSafeSearch(boolean forceSafeSearch) {
        this.forceSafeSearch = forceSafeSearch;
    }
}
