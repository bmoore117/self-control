package com.hyperion.selfcontrol.backend.config;

import com.hyperion.selfcontrol.backend.Credentials;
import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;

import java.util.HashSet;
import java.util.Set;

public class Config {

    private Long delay;
    private Set<Credentials> credentials;
    private State state;
    private Boolean hallPassUsed;
    private Bedtimes bedtimes;

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
        if (delay == null) {
            delay = 0L;
        }
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay == null ? 0 : delay;
    }

    public Set<Credentials> getCredentials() {
        if (credentials == null) {
            credentials = new HashSet<>();
        }
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

    public Bedtimes getBedtimes() {
        if (bedtimes == null) {
            bedtimes = new Bedtimes();
        }
        return bedtimes;
    }

    public void setBedtimes(Bedtimes bedtimes) {
        this.bedtimes = bedtimes;
    }
}
