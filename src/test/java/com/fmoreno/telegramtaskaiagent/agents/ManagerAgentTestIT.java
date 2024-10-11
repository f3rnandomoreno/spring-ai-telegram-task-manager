package com.fmoreno.telegramtaskaiagent.agents;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

// TODO fix the test with the RelevancyEvaluator
@Log4j2
public class ManagerAgentTestIT extends CommonTestIT {

    @Autowired
    private ManagerAgent managerAgent;

    @MockBean
    private NL2SQLAgent nl2SQLAgent;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        // No need for RelevancyEvaluator in this context
    }

    @Test
    void testCreateTask() {
        String userMessage = "Crea la tarea jugar al basket y asígnala a Fernando";
        String sql = "INSERT INTO tasks (description, assignee) VALUES ('jugar al basket', 'Fernando')";
        String executionResult = "Task created successfully";

        when(nl2SQLAgent.processNaturalLanguageToSQL(userMessage)).thenReturn(sql);
        when(taskService.executeSQLQuery(sql)).thenReturn(executionResult);

        String response = managerAgent.receiveMessageUser(userMessage, sql, executionResult);

        log.info("Response: {}", response);
        assertNotNull(response);
        assertTrue(response.contains("creado la tarea"));
        assertTrue(response.contains("jugar al basket"));
        assertTrue(response.contains("Fernando"));
    }

    @Test
    void testViewTask() {
        String userMessage = "Muéstrame mis tareas pendientes";
        String sql = "SELECT * FROM tasks WHERE status = 'pending'";
        String executionResult = "1. Jugar al basket (Fernando)\n2. Comprar groceries (María)\n3. Llamar al médico (Juan)";

        when(nl2SQLAgent.processNaturalLanguageToSQL(userMessage)).thenReturn(sql);
        when(taskService.executeSQLQuery(sql)).thenReturn(executionResult);

        String response = managerAgent.receiveMessageUser(userMessage, sql, executionResult);
        log.info("Response: {}", response);

        assertNotNull(response);
        assertTrue(response.contains("tareas pendientes"));
        assertTrue(response.contains("Jugar al basket"));
        assertTrue(response.contains("Comprar groceries"));
        assertTrue(response.contains("Llamar al médico"));
    }

    @Test
    void testModifyTask() {
        String userMessage = "Modifica la tarea 'Comprar groceries' y cámbiala a 'Comprar verduras'";
        String sql = "UPDATE tasks SET description = 'Comprar verduras' WHERE description = 'Comprar groceries'";
        String executionResult = "1 row(s) affected";

        when(nl2SQLAgent.processNaturalLanguageToSQL(userMessage)).thenReturn(sql);
        when(taskService.executeSQLQuery(sql)).thenReturn(executionResult);

        String response = managerAgent.receiveMessageUser(userMessage, sql, executionResult);
        log.info("Response: {}", response);

        assertNotNull(response);
        assertTrue(response.contains("modificado la tarea"));
        assertTrue(response.contains("Comprar groceries"));
        assertTrue(response.contains("Comprar verduras"));
    }

    @Test
    void testDeleteTask() {
        String userMessage = "Elimina la tarea 'Llamar al médico'";
        String sql = "DELETE FROM tasks WHERE description = 'Llamar al médico'";
        String executionResult = "1 row(s) affected";

        when(nl2SQLAgent.processNaturalLanguageToSQL(userMessage)).thenReturn(sql);
        when(taskService.executeSQLQuery(sql)).thenReturn(executionResult);

        String response = managerAgent.receiveMessageUser(userMessage, sql, executionResult);

        assertNotNull(response);
        assertTrue(response.contains("eliminado la tarea"));
        assertTrue(response.contains("Llamar al médico"));
    }

    @Test
    void testErrorHandling() {
        String userMessage = "Crea una tarea inválida";
        String sql = "INSERT INTO tasks (invalid_column) VALUES ('invalid_value')";
        String executionResult = "Error: column 'invalid_column' does not exist";

        when(nl2SQLAgent.processNaturalLanguageToSQL(userMessage)).thenReturn(sql);
        when(taskService.executeSQLQuery(sql)).thenReturn(executionResult);

        String response = managerAgent.receiveMessageUser(userMessage, sql, executionResult);

        assertNotNull(response);
        assertTrue(response.contains("error"));
        assertTrue(response.contains("No se pudo crear la tarea"));
    }
}
