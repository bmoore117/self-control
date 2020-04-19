package com.hyperion.selfcontrol.backend;

public abstract class AbstractFilterCategory {

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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTheme() {
        return theme;
    }
}
