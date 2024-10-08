package com.fmoreno.telegramtaskaiagent.agents;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class NL2SQLAgentTestIT extends CommonTestIT {

    @Autowired
    private NL2SQLAgent nl2SQLAgent;

    @Test
    void testCreateTask() {
        String input = "Crear una nueva tarea comprar leche asignada a Juan";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("insert into task"));
        assertTrue(result.toLowerCase().contains("'comprar leche'"));
        assertTrue(result.toLowerCase().contains("'juan'"));
    }

    @Test
    void testCreateTaskWithStatus() {
        String input = "Agregar tarea llamar al cliente asignada a María con estado en progreso";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("insert into task"));
        assertTrue(result.toLowerCase().contains("'llamar al cliente'"));
        assertTrue(result.toLowerCase().contains("'maría'"));
        assertTrue(result.toLowerCase().contains("'en progreso'"));
    }

    @Test
    void testReadAllTasks() {
        String input = "Mostrar todas las tareas";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("select * from task"));
    }

    @Test
    void testReadTasksWithCondition() {
        String input = "Listar tareas asignadas a Pedro";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("select * from task"));
        assertTrue(result.toLowerCase().contains("where assigned_to = 'pedro'"));
    }

    @Test
    void testUpdateTaskStatus() {
        String input = "Actualizar el estado de la tarea 1 a completada";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("update task"));
        assertTrue(result.toLowerCase().contains("set status = 'completada'"));
        assertTrue(result.toLowerCase().contains("where id = 1"));
    }

    @Test
    void testUpdateTaskAssignee() {
        String input = "Cambiar la asignación de la tarea 2 a Ana";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("update task"));
        assertTrue(result.toLowerCase().contains("set assigned_to = 'ana'"));
        assertTrue(result.toLowerCase().contains("where id = 2"));
    }

    @Test
    void testDeleteTask() {
        String input = "Eliminar la tarea 3";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("delete from task"));
        assertTrue(result.toLowerCase().contains("where id = 3"));
    }

    @Test
    void testDeleteTasksWithCondition() {
        String input = "Borrar todas las tareas completadas";
        String result = nl2SQLAgent.processNaturalLanguageToSQL(input);
        assertTrue(result.toLowerCase().contains("delete from task"));
        assertTrue(result.toLowerCase().contains("where status = 'completada'"));
    }

    @Test
    void testProcessSQLReview() {
        String sqlQuery = "SELECT * FORM task WHERE status = 'Pendiente'";
        String result = nl2SQLAgent.processSQLReview(sqlQuery);
        assertEquals("SELECT * FROM task WHERE status = 'Pendiente'", result.trim());
    }
}
