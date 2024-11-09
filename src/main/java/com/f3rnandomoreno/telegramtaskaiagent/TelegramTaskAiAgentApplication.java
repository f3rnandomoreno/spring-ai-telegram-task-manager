package com.f3rnandomoreno.telegramtaskaiagent;

import com.f3rnandomoreno.telegramtaskaiagent.config.DotEnvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramTaskAiAgentApplication {
    
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(TelegramTaskAiAgentApplication.class);
    app.addInitializers(new DotEnvInitializer());
    app.run(args);
  }

}
