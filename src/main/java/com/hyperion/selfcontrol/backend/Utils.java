package com.hyperion.selfcontrol.backend;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static Thread shutdownHook;
    private static String pid;

    public static <R> Supplier<R> composeWithDriver(Function<WebDriver, R> function) {
        return () -> {
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            WebDriver driver = null;
            try {
                driver = new RemoteWebDriver(
                        new URL("http://0.0.0.0:4444/wd/hub"),
                        capabilities);

                return function.apply(driver);
            } catch (MalformedURLException ex) {
                log.error("Malformed selenium host url", ex);
            } finally {
                if (driver != null) {
                    driver.close();
                }
            }
            return null;
        };
    }

    public static Runnable composeWithDriver(Consumer<WebDriver> function) {
        return () -> {
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            WebDriver driver = null;
            try {
                driver = new RemoteWebDriver(
                        new URL("http://0.0.0.0:4444/wd/hub"),
                        capabilities);

                function.accept(driver);
            } catch (MalformedURLException ex) {
                log.error("Malformed selenium host url", ex);
            } finally {
                if (driver != null) {
                    driver.close();
                }
            }
        };
    }

    public static int changePassword(String newPassword) {
        if (!Files.exists(Paths.get("changePassword.ps1"))) {
            log.warn("Password script not found, unpacking again");
            Resource resource = new ClassPathResource("changePassword.ps1");
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, Paths.get("changePassword.ps1"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Error unpacking changePassword from classpath", e);
                return -1;
            }
        }

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File("."));
            builder.command("powershell.exe", "-File", "changePassword.ps1", "-password", newPassword);

            Process p = builder.start();
            p.waitFor();

            try (InputStream stdOut = p.getInputStream(); InputStream stdErr = p.getErrorStream()) {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut));
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
                reader = new BufferedReader(new InputStreamReader(stdErr));
                while ((line = reader.readLine()) != null) {
                    log.error(line);
                }
            }

            return p.exitValue();
        } catch (IOException | InterruptedException e) {
            log.error("Error running changePassword.ps1", e);
        }

        return -1;
    }

    public static String launchAdminConsole() throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File("."));
        builder.command("powershell.exe", "-File", "startConsole.ps1");

        Process p = builder.start();
        p.waitFor();

        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream stdOut = p.getInputStream(); InputStream stdErr = p.getErrorStream()) {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut));
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader = new BufferedReader(new InputStreamReader(stdErr));
            while ((line = reader.readLine()) != null) {
                log.error(line);
            }
        }

        pid = stringBuilder.toString();
        log.info("Started console process " + pid);
        return pid;
    }

    public static void addShutdownHook() {
        /*
            Flow: launch admin console, expose admin password.
            Hide NN password in RAM, delete from file.
            Add shutdown hook to run script:
                args: user, pid
                Make sure PID is not running
                Make sure user does not have read access
                Change admin password
                write file
            for read access, best to move away from root user folder, and to single app folder,
            so permissions checks and sets don't take long

            As a safety precaution, make sure you can get in to pluckeye, and document in OneNote.
            Then set a 30 minute timer, after which the above script will be run, to ensure we don't get tripped up
            by power loss or unexpected restarts. Basically it just saves the hassle of going to pluckeye lockbox to
            restore NN password.
         */
        if (shutdownHook == null) {
            shutdownHook = new Thread(Utils::resetFile);
        }

        Runtime.getRuntime().addShutdownHook(shutdownHook);
        log.info("Added shutdown hook");
    }

    public static void resetFile() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File("."));
        builder.command("powershell.exe", "-File", "resetFile.ps1", "-filePath", CredentialService.FILE_LOCATION, "-processId", pid);

        try {
            Process p = builder.start();
            p.waitFor();

            try (InputStream stdOut = p.getInputStream(); InputStream stdErr = p.getErrorStream()) {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut));
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
                reader = new BufferedReader(new InputStreamReader(stdErr));
                while ((line = reader.readLine()) != null) {
                    log.error(line);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error running resetFile.ps1", e);
        }
    }

    public static void removeShutdownHook() {
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }
    }

    public static boolean isLocalAdminActive() {
        return shutdownHook != null;
    }
}
