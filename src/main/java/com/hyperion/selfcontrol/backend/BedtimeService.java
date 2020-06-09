package com.hyperion.selfcontrol.backend;

import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class BedtimeService {

    private static final Logger log = LoggerFactory.getLogger(BedtimeService.class);

    private final ConfigService configService;

    @Autowired
    public BedtimeService(ConfigService configService) {
        this.configService = configService;
    }

    /*
     * "0 0 * * * *" = the top of every hour of every day.
     * "10 * * * * *" = every ten seconds.
     * "0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
     * "0 0 8,10 * * *" = 8 and 10 o'clock of every day.
     * "0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.
     * "0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
     * "0 0 0 25 12 ?" = every Christmas Day at midnight
     * second, minute, hour, day of month, month, day(s) of week
     */
    // @Scheduled(cron = "0 0 5 * * *")
    public void reEnableInternetIfEligible() {
        Bedtimes bedtimes = configService.getConfig().getBedtimes();

        LocalDate date = LocalDate.now();
        DayOfWeek dow = date.getDayOfWeek();

        LocalTime cutoffTime;
        if ("sunday".equalsIgnoreCase(dow.toString())) {
            cutoffTime = bedtimes.getSunday();
        } else if ("monday".equalsIgnoreCase(dow.toString())) {
            cutoffTime = bedtimes.getMonday();
        } else if ("tuesday".equalsIgnoreCase(dow.toString())) {
            cutoffTime = bedtimes.getTuesday();
        } else if ("wednesday".equalsIgnoreCase(dow.toString())) {
            cutoffTime = bedtimes.getWednesday();
        } else if ("thursday".equalsIgnoreCase(dow.toString())) {
            cutoffTime = bedtimes.getThursday();
        } else if ("friday".equalsIgnoreCase(dow.toString())) {
            cutoffTime = bedtimes.getFriday();
        } else {
            cutoffTime = bedtimes.getSaturday();
        }

        LocalTime fiveAM = LocalTime.MIDNIGHT.plusHours(5);
        if (LocalTime.now().isAfter(fiveAM) && (cutoffTime == null || LocalTime.now().isBefore(cutoffTime))) {
            Utils.toggleInternet(true);
        }
    }
}
