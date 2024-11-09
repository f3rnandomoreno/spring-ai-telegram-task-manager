package com.f3rnandomoreno.telegramtaskaiagent.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Log4j2
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();
        String envFileName = ".env";
        if (activeProfiles.length > 0) {
            String activeProfile = activeProfiles[0];
            envFileName = ".env-" + activeProfile;
            log.debug("Using .env file for profile: {}", envFileName);
        }

        try {
            Dotenv dotenv = Dotenv.configure()
                    .filename(envFileName)
                    .load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                log.trace("Loaded environment variable: {}", entry.getKey());
            });

            log.info("DotEnv initialized successfully using file: {}", envFileName);
        } catch (Exception e) {
            log.warn("Failed to load {}: {}. Falling back to default .env", envFileName, e.getMessage());
            try {
                Dotenv defaultDotenv = Dotenv.load();
                defaultDotenv.entries().forEach(entry -> {
                    System.setProperty(entry.getKey(), entry.getValue());
                });
                log.info("Fallback to default .env successful");
            } catch (Exception defaultE) {
                log.error("Failed to load default .env: {}", defaultE.getMessage());
            }
        }
    }
}