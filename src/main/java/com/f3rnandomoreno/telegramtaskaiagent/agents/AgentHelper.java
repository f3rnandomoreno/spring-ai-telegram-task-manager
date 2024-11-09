package com.f3rnandomoreno.telegramtaskaiagent.agents;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AgentHelper {

    // check if sql is insert, update or delete
    public static boolean isInsertOrUpdateOrDelete(String sqlQuery) {
        String lowerCaseQuery = sqlQuery.toLowerCase().trim();
        return lowerCaseQuery.startsWith("insert") || lowerCaseQuery.startsWith("update") || lowerCaseQuery.startsWith("delete");
    }
}
