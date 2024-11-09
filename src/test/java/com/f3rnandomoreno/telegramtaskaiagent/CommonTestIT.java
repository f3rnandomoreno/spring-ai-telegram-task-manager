package com.f3rnandomoreno.telegramtaskaiagent;

import com.f3rnandomoreno.telegramtaskaiagent.config.DotEnvInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = {DotEnvInitializer.class})
@ActiveProfiles("test")
public class CommonTestIT {}
