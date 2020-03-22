package com.hyperion.selfcontrol.backend;

public class FilterCategory {
    private String name;
    private String status;
    private String theme;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTheme() {
        return theme;
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
