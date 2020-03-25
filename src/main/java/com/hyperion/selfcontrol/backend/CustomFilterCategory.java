package com.hyperion.selfcontrol.backend;

public class CustomFilterCategory {
    private String name;
    private String status;
    private String theme;

    public CustomFilterCategory(String name, String status) {
        this.name = name;
        this.status = status;
        if ("inactive".equals(status.toLowerCase())) {
            theme = "badge";
        } else if ("block".equals(status.toLowerCase())) {
            theme = "badge success";
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
        return "CustomFilterCategory{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}
