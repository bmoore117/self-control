package com.hyperion.selfcontrol.backend;

public class CustomFilterCategory extends AbstractFilterCategory {

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

    @Override
    public String toString() {
        return "CustomFilterCategory{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}
