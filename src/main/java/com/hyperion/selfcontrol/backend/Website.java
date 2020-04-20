package com.hyperion.selfcontrol.backend;

import java.util.Objects;

public class Website {

    private String name;

    public Website(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Website website = (Website) o;
        return Objects.equals(name, website.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
