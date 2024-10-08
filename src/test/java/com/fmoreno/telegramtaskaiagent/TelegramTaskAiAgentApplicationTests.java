package com.fmoreno.telegramtaskaiagent;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
class TelegramTaskAiAgentApplicationTests extends CommonTestIT {

	@Test
	void contextLoads() {
	}

}
