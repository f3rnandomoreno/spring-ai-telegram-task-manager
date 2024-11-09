package com.f3rnandomoreno.telegramtaskaiagent;

import com.f3rnandomoreno.telegramtaskaiagent.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
class TelegramTaskAiAgentApplicationTests extends CommonTestIT {

	@Test
	void contextLoads() {
	}

}
