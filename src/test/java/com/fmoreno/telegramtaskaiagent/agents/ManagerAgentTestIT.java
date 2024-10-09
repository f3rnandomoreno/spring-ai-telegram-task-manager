package com.fmoreno.telegramtaskaiagent.agents;

import static org.junit.jupiter.api.Assertions.*;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import java.util.Collections;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class ManagerAgentTestIT extends CommonTestIT {

    @Autowired
    private ManagerAgent managerAgent;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    private RelevancyEvaluator relevancyEvaluator;

    @BeforeEach
    void setUp() {
        relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));
    }

    @Test
    void testCreateTask() {
        String userMessage = "Crea la tarea jugar al basket y asígnala a Fernando";
    String expectedResponse =
        "He creado la tarea \"jugar al basket\" y la he asignado a Fernando. ¿Necesitas algo más?";

        testRelevancy(userMessage, expectedResponse);
    }

    @Test
    void testViewTask() {
        String userMessage = "Muéstrame mis tareas pendientes";
        String expectedResponse = "Aquí tienes tus tareas pendientes:\n1. Jugar al basket\n2. Comprar groceries\n3. Llamar al médico";
        
        testRelevancy(userMessage, expectedResponse);
    }

    @Test
    void testModifyTask() {
        String userMessage = "Modifica la tarea 'Comprar groceries' y cámbiala a 'Comprar verduras'";
        String expectedResponse = "He modificado la tarea. 'Comprar groceries' ahora es 'Comprar verduras'.";
        
        testRelevancy(userMessage, expectedResponse);
    }

    @Test
    void testDeleteTask() {
        String userMessage = "Elimina la tarea 'Llamar al médico'";
        String expectedResponse = "He eliminado la tarea 'Llamar al médico' de tu lista.";
        
        testRelevancy(userMessage, expectedResponse);
    }

    @Test
    void testAmbiguousRequest() {
        String userMessage = "Necesito hacer algo importante";
        String expectedResponse = "Disculpa, tu solicitud es un poco ambigua. ¿Podrías especificar si quieres crear, ver, modificar o eliminar una tarea en particular?";
        
        testRelevancy(userMessage, expectedResponse);
    }

    private void testRelevancy(String userMessage, String expectedResponse) {
        log.info("Testing user message: {}", userMessage);
        String result = managerAgent.receiveMessageUser(userMessage);

        log.info("Response: {}", result);
        EvaluationRequest evaluationRequest = new EvaluationRequest(userMessage, Collections.emptyList(), result);
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        
        assertTrue(evaluationResponse.isPass(), "Response is not relevant to the question");
        assertTrue(result.contains(expectedResponse), "Response does not contain expected content");
    }

}
