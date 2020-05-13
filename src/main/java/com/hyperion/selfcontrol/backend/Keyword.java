package com.hyperion.selfcontrol.backend;

import java.util.Objects;

public class Keyword {

    private String value;

    public Keyword() {}

    public Keyword(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keyword keyword = (Keyword) o;
        return Objects.equals(value, keyword.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
