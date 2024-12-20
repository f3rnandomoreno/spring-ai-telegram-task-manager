package com.f3rnandomoreno.telegramtaskaiagent.agents;

import com.f3rnandomoreno.telegramtaskaiagent.CommonTestIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class NL2SQLAgentTestIT extends CommonTestIT {

    @Autowired
    private NL2SQLAgent nl2SQLAgent;

    @Test
    void testCreatetasks() {
        String input = "Crear una nueva tarea comprar leche asignada a Juan";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Juan");
        assertThat(result.toLowerCase()).contains("insert into tasks");
        assertThat(result.toLowerCase()).contains("'comprar leche'");
        assertThat(result.toLowerCase()).contains("'juan'");
    }

    @Test
    void testCreateTaskWithStatus() {
        String input = "Agregar tarea llamar al cliente asignada a María con estado en progreso";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("insert into tasks");
        assertThat(result.toLowerCase()).contains("'llamar al cliente'");
        assertThat(result.toLowerCase()).contains("'maría'");
        assertThat(result.toLowerCase()).contains("'in_progress'");
    }

    @Test
    void testReadAllTasks() {
        String input = "Mostrar todas las tareas";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("select * from tasks");
    }

    @Test
    void testReadTasksWithCondition() {
        String input = "Listar tareas asignadas a Pedro";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("select * from tasks");
        assertThat(result.toLowerCase()).contains("where assignee = 'pedro'");
    }

    @Test
    void testUpdateTaskStatus() {
        String input = "Actualizar el estado de la tarea 1 a completada";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("update tasks");
        assertThat(result.toLowerCase()).contains("set status = 'done'");
        assertThat(result.toLowerCase()).contains("where id = 1");
    }

    @Test
    void testUpdateTaskAssignee() {
        String input = "Cambiar la asignación de la tarea 2 a Ana";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("update tasks");
        assertThat(result.toLowerCase()).contains("set assignee = 'ana'");
        assertThat(result.toLowerCase()).contains("where id = 2");
    }

    @Test
    void testDeleteTask() {
        String input = "Eliminar la tarea 3";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("delete from tasks");
        assertThat(result.toLowerCase()).contains("where id = 3");
    }

    @Test
    void testDeleteTasksWithCondition() {
        String input = "Borrar todas las tareas completadas";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Usuario");
        assertThat(result.toLowerCase()).contains("delete from tasks");
        assertThat(result.toLowerCase()).contains("where status = 'done'");
    }

    @Test
    void testCreateTaskAssignedToFernando() {
        String input = "Creame una nueva tarea revisar el código";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "Fernando");
        assertThat(result.toLowerCase()).contains("insert into tasks");
        assertThat(result.toLowerCase()).contains("'revisar el código'");
        assertThat(result.toLowerCase()).contains("'fernando'");
    }

    @Test
    void testCreateTaskAssignedToFernandoWithMaria() {
        String input = "Crear una nueva tarea revisar el código asignada a Fernando";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input, "María");
        assertThat(result.toLowerCase()).contains("insert into tasks");
        assertThat(result.toLowerCase()).contains("'revisar el código'");
        assertThat(result.toLowerCase()).contains("'fernando'");
    }
}
