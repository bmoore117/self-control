package com.hyperion.selfcontrol.backend;

public class Credentials {

    private String username;
    private String password;
    private String tag;

    public Credentials(String password, String tag, String username) {
        this.username = username;
        this.password = password;
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
