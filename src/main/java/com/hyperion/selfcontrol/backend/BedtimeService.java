package com.hyperion.selfcontrol.backend;

import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class BedtimeService {

    private static final Logger log = LoggerFactory.getLogger(BedtimeService.class);

    private final TaskScheduler taskScheduler;
    private final ConfigService configService;

    private final Map<DayOfWeek, ScheduledFuture<?>> scheduledCutoffTimes;

    @Autowired
    public BedtimeService(ConfigService configService, TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.configService = configService;
        scheduledCutoffTimes = new HashMap<>();
    }

    public void scheduleToday(Bedtimes bedtimes) {
        log.info("Setting new internet cutoff time");
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

        ScheduledFuture<?> cutoffTask = scheduledCutoffTimes.get(dow);
        if (cutoffTask != null && !cutoffTask.isCancelled()) {
            cutoffTask.cancel(true);
            log.info("Cancelled old internet shutoff task");
        }

        // turn internet off at target time
        Instant instant = cutoffTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
        if (instant.isBefore(Instant.now())) {
            log.info("Toggling internet with immediate effect");
            Utils.toggleInternet(false);
        } else {
            Date time = Date.from(instant);
            ScheduledFuture<?> task = taskScheduler.schedule(() -> {
                log.info("Toggling internet on schedule");
                Utils.toggleInternet(false);
            }, time);
            scheduledCutoffTimes.put(dow, task);
            log.info("Internet cutoff scheduled for {}", time);
        }
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
    @Scheduled(cron = "0 0 5 * * *")
    public void reEnableInternet() {
        log.info("Toggling internet back on");
        Utils.toggleInternet(true);

        try {
            configService.refreshFile();
            scheduleToday(configService.getConfig().getBedtimes());
        } catch (IOException e) {
            log.error("Error refreshing settings from " + ConfigService.FILE_LOCATION, e);
        }
    }
}
