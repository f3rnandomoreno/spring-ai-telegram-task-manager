package com.fmoreno.telegramtaskaiagent;

import com.fmoreno.telegramtaskaiagent.config.DotEnvInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = {DotEnvInitializer.class})
public class ITCommonTest {}
