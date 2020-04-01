package com.hyperion.selfcontrol.backend;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CredentialService {

    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

    public static final String FILE_LOCATION = "C:\\Users\\ben-local\\credentials.json";

    private JSONObject fileContents;

    public CredentialService() throws IOException {
        String join = String.join("\n", Files.readAllLines(Paths.get(FILE_LOCATION)));
        fileContents = new JSONObject(join);
        if (!fileContents.has("delay")) {
            fileContents.put("delay", 0);
            writeFile();
        }
    }

    public String getNetNannyUsername() {
        return fileContents.getString("net-nanny-username");
    }

    public String getNetNannyPassword() {
        return fileContents.getString("net-nanny-password");
    }

    public String getLocalAdminUsername() {
        return fileContents.getString("local-admin-username");
    }

    public String getLocalAdminPassword() {
        return fileContents.getString("local-admin-password");
    }

    public void setCredentials(Credentials credentials) {
        fileContents.put(credentials.getTag() + "-username", credentials.getUsername());
        fileContents.put(credentials.getTag() + "-password", credentials.getPassword());
        fileContents.put(credentials.getTag() + "-tag", credentials.getTag());
        writeFile();
    }

    public List<Credentials> getCredentials() {
        if (isEnabled()) {
            List<String> collect = fileContents.keySet().stream().filter(k -> k.endsWith("username") || k.endsWith("password") || k.endsWith("tag")).sorted().collect(Collectors.toList());
            List<Credentials> results = new ArrayList<>();
            for (int i = 0; i < collect.size(); i += 3) {
                // in the sort, password, tag, username
                results.add(new Credentials(fileContents.getString(collect.get(i)), fileContents.getString(collect.get(i + 1)), fileContents.getString(collect.get(i + 2))));
            }

            return results;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isEnabled() {
        return 0 == fileContents.getLong("delay");
    }

    public long getDelay() {
        return fileContents.getLong("delay");
    }

    public void setDelay(long delayInMillis) {
        Function<Long, Void> writeDelay = aLong -> {
            fileContents.put("delay", delayInMillis);
            return null;
        };

        runWithDelay("Write Delay Timer", writeDelay, delayInMillis);
    }

    private void writeFile() {
        try {
            Files.write(Paths.get(FILE_LOCATION), fileContents.toString().getBytes());
        } catch (IOException e) {
            log.error("Error writing new delay", e);
        }
    }

    public <T, R> void runWithDelay(String name, Function<T, R> function, T input) {
        long delay = fileContents.getLong("delay");
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
}
