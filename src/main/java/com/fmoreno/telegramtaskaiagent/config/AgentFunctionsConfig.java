package com.fmoreno.telegramtaskaiagent.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.function.Function;

@Configuration
@Log4j2
public class AgentFunctionsConfig {

    @Bean
    @Description("Modifica el estado o detalles de una tarea existente.")
    public Function<Map<String, Object>, String> modificarTarea() {
        return arguments -> {
            log.info("Modificando tarea con argumentos: {}", arguments);
            return "Tarea modificada con éxito.";
        };
    }

    @Bean
    @Description("Elimina una tarea existente utilizando su ID o descripción.")
    public Function<Map<String, Object>, String> eliminarTarea() {
        return arguments -> {
            log.info("Eliminando tarea con argumentos: {}", arguments);
            return "Tarea eliminada con éxito.";
        };
    }

    @Bean
    @Description("Crea una nueva tarea con una descripción y opcionalmente asignada a alguien.")
    public Function<Map<String, Object>, String> crearTarea() {
        return arguments -> {
            log.info("Creando tarea con argumentos: {}", arguments);
            return "Tarea creada con éxito.";
        };
    }

    @Bean
    @Description("Recupera y muestra una lista de tareas, opcionalmente filtradas por estado, asignado o un filtro general.")
    public Function<Map<String, Object>, String> verTareas() {
        return arguments -> {
            log.trace("Visualizando tareas con argumentos: {}", arguments);
            return "Tareas visualizadas con éxito.";
        };
    }

}