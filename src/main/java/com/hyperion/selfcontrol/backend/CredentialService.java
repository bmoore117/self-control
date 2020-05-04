package com.hyperion.selfcontrol.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperion.selfcontrol.backend.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CredentialService {

    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

    public static String FILE_LOCATION = "C:\\Users\\ben-local\\self-control\\credentials.json";
    public static final String STOCK_PASSWORD = "P@ssw0rd";

    private Config config;
    private final ObjectMapper mapper;

    public CredentialService() throws IOException {
        mapper = new ObjectMapper();
        try {
            config = mapper.readValue(new File(FILE_LOCATION), Config.class);
        } catch (FileNotFoundException e) {
            FILE_LOCATION = "C:\\Users\\moore\\self-control\\credentials.json";
            config = mapper.readValue(new File(FILE_LOCATION), Config.class);
        }
        log.info("Using " + FILE_LOCATION);
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void resetHallPassForTheWeek() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        boolean isWeekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .contains(now.getDayOfWeek());

        LocalDateTime fivePMOnFriday = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 17, 0);
        boolean afterFiveOnFriday = EnumSet.of(DayOfWeek.FRIDAY).contains(now.getDayOfWeek()) && now.isAfter(fivePMOnFriday);

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
        if (isEnabled()) {
            return new ArrayList<>(config.getCredentials());
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isEnabled() {
        return 0 == config.getDelay();
    }

    public long getDelay() {
        return config.getDelay();
    }

    public void setDelay(long delayInMillis) {
        Function<Long, Void> writeDelay = aLong -> {
            config.setDelay(delayInMillis);
            return null;
        };

        runWithDelay("Write Delay Timer", writeDelay, delayInMillis);
    }

    public void writeFile() {
        try {
            Files.write(Paths.get(FILE_LOCATION), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config).getBytes());
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
        timer.schedule(task, getDelay());
    }

    public Config getConfig() {
        return config;
    }
}
