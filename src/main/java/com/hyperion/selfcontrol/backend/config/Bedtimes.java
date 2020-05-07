package com.hyperion.selfcontrol.backend.config;

import java.time.LocalTime;

public class Bedtimes {

    private LocalTime sunday;
    private LocalTime monday;
    private LocalTime tuesday;
    private LocalTime wednesday;
    private LocalTime thursday;
    private LocalTime friday;
    private LocalTime saturday;

    public LocalTime getSunday() {
        return sunday;
    }

    public void setSunday(LocalTime sunday) {
        this.sunday = sunday;
    }

    public LocalTime getMonday() {
        return monday;
    }

    public void setMonday(LocalTime monday) {
        this.monday = monday;
    }

    public LocalTime getTuesday() {
        return tuesday;
    }

    public void setTuesday(LocalTime tuesday) {
        this.tuesday = tuesday;
    }

    public LocalTime getWednesday() {
        return wednesday;
    }

    public void setWednesday(LocalTime wednesday) {
        this.wednesday = wednesday;
    }

    public LocalTime getThursday() {
        return thursday;
    }

    public void setThursday(LocalTime thursday) {
        this.thursday = thursday;
    }

    public LocalTime getFriday() {
        return friday;
    }

    public void setFriday(LocalTime friday) {
        this.friday = friday;
    }

    public LocalTime getSaturday() {
        return saturday;
    }

    public void setSaturday(LocalTime saturday) {
        this.saturday = saturday;
    }
}
