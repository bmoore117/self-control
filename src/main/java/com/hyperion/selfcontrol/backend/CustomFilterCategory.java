package com.hyperion.selfcontrol.backend;

import java.util.List;

public class CustomFilterCategory extends AbstractFilterCategory {

    public List<Keyword> keywords;

    public static final String INACTIVE = "inactive";

    public CustomFilterCategory(String name, String status) {
        this(name, status, null);
    }

    public CustomFilterCategory(String name, String status, List<Keyword> keywords) {
        this.keywords = keywords;
        this.name = name;
        this.status = status;
        if (INACTIVE.equals(status.toLowerCase())) {
            theme = "badge";
        } else if ("block".equals(status.toLowerCase())) {
            theme = "badge success";
        } else {
            theme = "badge error";
        }
    }

    @Override
    public String toString() {
        return "CustomFilterCategory{" +
                "keywords=" + keywords +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }
}
