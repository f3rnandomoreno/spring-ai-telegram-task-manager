package com.fmoreno.telegramtaskaiagent.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class DotEnvConfig {

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.load();

        for (DotenvEntry entry : dotenv.entries()) {
            log.info("Setting environment variable: {}", entry.getKey());
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
