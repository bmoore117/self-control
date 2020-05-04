package com.hyperion.selfcontrol.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperion.selfcontrol.backend.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

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

    public String getNetNannyUsername() {
        return config.getCredentials().get("net-nanny").getUsername();
    }

    public String getNetNannyPassword() {
        return config.getCredentials().get("net-nanny").getPassword();
    }

    public void setCredentials(Credentials credentials, String tag) {
        config.getCredentials().put(tag, credentials);
        writeFile();
    }

    public Optional<Credentials> getLocalAdmin() {
        return config.getCredentials().entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("local"))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public List<Credentials> getCredentials() {
        if (isEnabled()) {
            Credentials credentials = config.getCredentials().get("net-nanny");
            Credentials withTag = new Credentials(credentials.getPassword(), credentials.getUsername(), "net-nanny");
            return Collections.singletonList(withTag);
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

    public void setDelayDirect(long delayInMillis) {
        config.setDelay(delayInMillis);
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

    public void liftNetNannyCredentialsToRAM() throws IOException {
        config = mapper.readValue(new File(FILE_LOCATION), Config.class);
        String password = null;
        for (Map.Entry<String, Credentials> entry : config.getCredentials().entrySet()) {
            if (entry.getKey().contains("net-nanny")) {
                password = entry.getValue().getPassword();
                entry.getValue().setPassword("null");
            }
        }

        writeFile();

        for (Map.Entry<String, Credentials> entry : config.getCredentials().entrySet()) {
            if (entry.getKey().contains("net-nanny")) {
                entry.getValue().setPassword(password);
            }
        }
    }
}
