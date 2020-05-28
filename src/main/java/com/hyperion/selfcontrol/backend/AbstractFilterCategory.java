package com.hyperion.selfcontrol.backend;

public abstract class AbstractFilterCategory {

    public static final String ALLOW = "allow";
    public static final String BLOCK = "block";

    protected String name;
    protected String status;
    protected String theme;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public abstract void setStatus(String status);

    public String getTheme() {
        return theme;
    }

    public void setThemeBlocked() {
        this.theme = "block";
    }

    public void setThemeAllow() {
        this.theme = "badge success";
    }

    public void setThemeError() {
        this.theme = "badge error";
    }
}
