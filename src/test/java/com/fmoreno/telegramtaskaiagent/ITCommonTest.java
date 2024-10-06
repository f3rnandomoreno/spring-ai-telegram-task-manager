package com.fmoreno.telegramtaskaiagent;

import com.fmoreno.telegramtaskaiagent.config.DotEnvInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = {DotEnvInitializer.class})
@ActiveProfiles("test")
public class ITCommonTest {}
