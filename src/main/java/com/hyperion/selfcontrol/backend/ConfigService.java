package com.hyperion.selfcontrol.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyperion.selfcontrol.backend.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    public static String FILE_LOCATION = "C:\\Users\\ben-local\\self-control";
    public static final String FILE_NAME = "config.json";
    public static final String STOCK_PASSWORD = "P@ssw0rd";

    private Config config;
    public static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public ConfigService() throws IOException {
        refreshFile();
    }

    public void refreshFile() throws IOException {
        try {
            File protectedLocation = new File(FILE_LOCATION);
            File protectedFile = new File(FILE_LOCATION + "\\" + FILE_NAME);
            if (!protectedFile.exists()) {
                config = new Config();
                protectedLocation.mkdirs();
                Files.write(Paths.get(protectedFile.getAbsolutePath()), mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(config).getBytes());
            } else {
                config = mapper.readValue(protectedFile, Config.class);
            }
        } catch (AccessDeniedException e) {
            FILE_LOCATION = "self-control";
            File unProtectedLocation = new File(FILE_LOCATION);
            File unProtectedFile = new File(FILE_LOCATION + "\\" + FILE_NAME);
            if (!unProtectedFile.exists()) {
                unProtectedLocation.mkdirs();
                Files.write(Paths.get(unProtectedFile.getAbsolutePath()), mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(config).getBytes());
            } else {
                config = mapper.readValue(unProtectedFile, Config.class);
            }
        }
        log.info("Loaded file from " + FILE_LOCATION);
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
    @Scheduled(cron = "0 0 0 * * MON")
    public void resetHallPassForTheWeek() {
        log.info("Entering resetHallPassForTheWeek");
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        boolean isWeekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .contains(now.getDayOfWeek());

        LocalDateTime fivePMOnFriday = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 17, 0);
        boolean afterFiveOnFriday = EnumSet.of(DayOfWeek.FRIDAY).contains(now.getDayOfWeek()) && now.isAfter(fivePMOnFriday);

        log.info("Hall pass used: {}", config.isHallPassUsed());
        log.info("isWeekend: {}", isWeekend);
        log.info("afterFiveOnFriday: {}", afterFiveOnFriday);
        if (config.isHallPassUsed() && !(isWeekend || afterFiveOnFriday)) {
            log.info("Resetting hall pass for the week");
            config.setHallPassUsed(false);
            writeFile();
        }
    }

    public Optional<Credentials> getLocalAdmin() {
        return config.getCredentials().stream()
                .filter(entry -> entry.getTag().endsWith("local"))
                .findFirst();
    }

    public boolean isHallPassUsed() {
        return config.isHallPassUsed();
    }

    public void setHallPassUsed() {
        config.setHallPassUsed(true);
        writeFile();
    }

    public Optional<Credentials> getNetNanny() {
        return config.getCredentials().stream()
                .filter(entry -> "net-nanny".equals(entry.getTag()))
                .findFirst();
    }

    public void setCredentials(Credentials credentials) {
        config.getCredentials().remove(credentials);
        config.getCredentials().add(credentials);
        writeFile();
    }

    public List<Credentials> getCredentials() {
        if (config.getCredentials() == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(config.getCredentials());
        }
    }

    public boolean isEnabled() {
        if (config.getDelay() != null) {
            return 0 == config.getDelay();
        } else {
            return true;
        }
    }

    public long getDelayMillis() {
        return config.getDelay();
    }

    public void setDelay(long delayInMillis) {
        config.setDelay(delayInMillis);
        writeFile();
    }

    public void writeFile() {
        try {
            Files.write(Paths.get(FILE_LOCATION + "\\" + FILE_NAME), mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(config).getBytes());
        } catch (IOException e) {
            log.error("Error writing new delay", e);
        }
    }

    public <T, R> void runWithDelay(String name, Function<T, R> function, T input) {
        long delay = config.getDelay();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("Running scheduled task: " + name);
                function.apply(input);
                writeFile();
            }
        };
        Timer timer = new Timer(name);
        log.info("Scheduling task: " + name);
        timer.schedule(task, delay);
    }

    public void runWithDelay(String name, Runnable action) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("Running scheduled task: " + name);
                action.run();
            }
        };
        Timer timer = new Timer(name);
        log.info("Scheduling task: " + name);
        timer.schedule(task, getDelayMillis());
    }

    public Config getConfig() {
        return config;
    }
}
