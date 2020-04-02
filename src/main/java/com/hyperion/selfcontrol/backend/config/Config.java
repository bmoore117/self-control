package com.hyperion.selfcontrol.backend.config;

import com.hyperion.selfcontrol.backend.Credentials;

import java.util.Map;

public class Config {

    private Long delay;
    private Map<String, Credentials> credentials;
    private State state;

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay == null ? 0 : delay;
    }

    public Map<String, Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Credentials> credentials) {
        this.credentials = credentials;
    }

    public State getState() {
        if (state == null) {
            state = new State();
        }
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
