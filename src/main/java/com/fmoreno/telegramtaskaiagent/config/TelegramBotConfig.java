package com.fmoreno.telegramtaskaiagent.config;

import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.client.TelegramClientConfig;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
@Log4j2
public class TelegramBotConfig {

  @Value("${telegram.bot.token}")
  protected String botToken;

  @Autowired ChatClient chatClient;

  @Autowired NL2SQLAgent nl2SQLAgent;

  @Autowired TaskService taskService;

  @Autowired ManagerAgent managerAgent;

  @PostConstruct
  public void init() {
    try {
      TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
      botsApplication.registerBot(
          botToken, new TelegramClientConfig(botToken, chatClient, nl2SQLAgent, taskService,managerAgent));
      log.trace("Bot registered successfully");
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }
}
