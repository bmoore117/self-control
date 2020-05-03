package com.hyperion.selfcontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Component
public class FileUnpacker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FileUnpacker.class);

    @Override
    public void run(ApplicationArguments args) {
        log.info("Unpacking password script");
        Resource resource = new ClassPathResource("changePassword.ps1");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Paths.get("changePassword.ps1"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error unpacking changePassword from classpath", e);
        }

        log.info("Unpacking resetFile script");
        resource = new ClassPathResource("resetFile.ps1");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Paths.get("resetFile.ps1"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error unpacking resetFile from classpath", e);
        }

        log.info("Unpacking startConsole script");
        resource = new ClassPathResource("startConsole.ps1");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Paths.get("startConsole.ps1"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error unpacking startConsole from classpath", e);
        }
    }
}
