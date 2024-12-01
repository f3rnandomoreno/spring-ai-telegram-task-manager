package com.f3rnandomoreno.telegramtaskaiagent.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        log.info("Verificando estructura de la tabla tasks...");
        addRemovedColumnIfNotExists();
    }

    private void addRemovedColumnIfNotExists() {
        try {
            // Verificamos si la columna existe usando DatabaseMetaData
            boolean columnExists = columnExists("tasks", "removed");
            
            if (!columnExists) {
                log.info("Añadiendo columna 'removed' a la tabla tasks...");
                jdbcTemplate.execute(
                    "ALTER TABLE tasks ADD COLUMN removed BOOLEAN DEFAULT FALSE NOT NULL"
                );
                log.info("Columna 'removed' añadida correctamente");
            } else {
                log.info("La columna 'removed' ya existe en la tabla tasks");
            }
        } catch (Exception e) {
            log.error("Error al verificar/añadir la columna 'removed': {}", e.getMessage());
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = jdbcTemplate.getDataSource().getConnection().getMetaData();
        ResultSet rs = meta.getColumns(null, null, tableName, columnName);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
} 