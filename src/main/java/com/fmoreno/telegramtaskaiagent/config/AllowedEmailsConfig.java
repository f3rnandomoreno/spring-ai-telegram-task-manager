package com.fmoreno.telegramtaskaiagent.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Arrays;
import java.util.List;

public class AllowedEmailsConfig {
    private static final Dotenv dotenv = Dotenv.load();
    public static final List<String> ALLOWED_EMAILS = Arrays.asList(dotenv.get("ALLOWED_EMAILS").split(","));
}
