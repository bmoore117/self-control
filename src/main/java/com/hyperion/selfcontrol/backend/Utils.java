package com.hyperion.selfcontrol.backend;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

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
}
