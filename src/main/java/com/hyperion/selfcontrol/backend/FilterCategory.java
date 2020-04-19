package com.hyperion.selfcontrol.backend;

public class FilterCategory extends AbstractFilterCategory {

    public FilterCategory(String name, String status) {
        this.name = name;
        this.status = status;
        if ("allow".equals(status.toLowerCase())) {
            theme = "badge success";
        } else if ("block".equals(status.toLowerCase())) {
            theme = "badge";
        } else {
            theme = "badge error";
        }
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return "FilterCategory{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}
