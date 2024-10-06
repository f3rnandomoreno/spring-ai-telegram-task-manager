package com.fmoreno.telegramtaskaiagent.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Log4j2
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        log.trace("DotEnv initialized successfully");
    }
}