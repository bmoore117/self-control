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
import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static final int MILLISECONDS_TO_NANOSECONDS = 1000000;

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

    public static int changeLocalAdminPassword(String newPassword) {
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
                    log.info(line);
                }
            }

            return p.exitValue();
        } catch (IOException | InterruptedException e) {
            log.error("Error running changePassword.ps1", e);
        }

        return -1;
    }

    public static String generatePassword() {
        int leftLimit = 33; // numeral '0'
        int rightLimit = 126; // character '~'
        int targetStringLength = 12;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static void toggleInternet(boolean on) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File("."));
            if (on) {
                builder.command("powershell.exe", "-File", "toggleInternet.ps1", "-enableInternet");
            } else {
                builder.command("powershell.exe", "-File", "toggleInternet.ps1");
            }

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
            log.error("Error running toggleInternet, with arg: " + on, e);
        }
    }

    public static LocalDateTime getCurrentTimePlusDelay(ConfigService configService) {
        return LocalDateTime.now().plusNanos(configService.getDelayMillis() * MILLISECONDS_TO_NANOSECONDS);
    }
}
