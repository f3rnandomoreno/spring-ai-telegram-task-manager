package com.fmoreno.telegramtaskaiagent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class TaskService {

    final JdbcTemplate jdbcTemplate;

    public String executeSQLQuery(String sqlQuery) {
        log.info("Executing SQL Query: {}", sqlQuery);
        String lowerCaseQuery = sqlQuery.toLowerCase().trim();
        
        if (lowerCaseQuery.startsWith("select")) {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            return formatSelectResults(results);
        } else {
            int rowsAffected = jdbcTemplate.update(sqlQuery);
            String action = determineAction(lowerCaseQuery);
            return String.format("%s realizada con éxito. Filas afectadas: %d", action, rowsAffected);
        }
    }

    private String formatSelectResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return "No se encontraron resultados.";
        }
        
        StringBuilder sb = new StringBuilder("Resultados:\n");
        for (Map<String, Object> row : results) {
            sb.append("- ");
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            sb.setLength(sb.length() - 2); // Remove last ", "
            sb.append("\n");
        }
        return sb.toString();
    }

    private String determineAction(String query) {
        if (query.startsWith("insert")) return "Inserción";
        if (query.startsWith("update")) return "Actualización";
        if (query.startsWith("delete")) return "Eliminación";
        return "Operación";
    }
}
