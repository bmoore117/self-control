package com.hyperion.selfcontrol.backend.config;

import com.hyperion.selfcontrol.backend.Credentials;

import java.util.List;
import java.util.Set;

public class Config {

    private Long delay;
    private Set<Credentials> credentials;
    private State state;
    private Boolean hallPassUsed;

    public Boolean isHallPassUsed() {
        if (hallPassUsed == null) {
            return false;
        }

        return hallPassUsed;
    }

    public void setHallPassUsed(Boolean hallPassUsed) {
        this.hallPassUsed = hallPassUsed;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay == null ? 0 : delay;
    }

    public Set<Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(Set<Credentials> credentials) {
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
