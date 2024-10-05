package com.fmoreno.telegramtaskaiagent;

import org.springframework.boot.SpringApplication;

public class TestTelegramTaskAiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.from(TelegramTaskAiAgentApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
