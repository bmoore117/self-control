package com.hyperion.selfcontrol.backend;

import java.util.List;

public class CustomFilterCategory extends AbstractFilterCategory {

    public List<String> keywords;

    public CustomFilterCategory(String name, String status) {
        this(name, status, null);
    }

    public CustomFilterCategory(String name, String status, List<String> keywords) {
        this.keywords = keywords;
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

    @Override
    public String toString() {
        return "CustomFilterCategory{" +
                "keywords=" + keywords +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}
