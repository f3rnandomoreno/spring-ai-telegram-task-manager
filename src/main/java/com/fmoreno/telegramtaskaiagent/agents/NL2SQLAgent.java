package com.fmoreno.telegramtaskaiagent.agents;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NL2SQLAgent {

    @Autowired
	private ChatClient chatClient;

	public String processNaturalLanguage(String input) {
		String prompt = "Eres un asistente que convierte instrucciones en lenguaje natural a consultas SQL para una base de datos de tareas.\n" +
				"La tabla 'task' tiene los siguientes campos:\n" +
				"- id (INTEGER, clave primaria, autoincremental)\n" +
				"- description (TEXT)\n" +
				"- assigned_to (TEXT)\n" +
				"- status (TEXT, puede ser 'Pendiente', 'En Progreso', 'Bloqueada', o 'Completada')\n" +
				"- created_at (DATETIME, valor por defecto CURRENT_TIMESTAMP)\n\n" +
				"Instrucción del usuario: " + input + "\n\n" +
				"Genera una consulta SQL válida basada en esta instrucción. No incluyas explicaciones, solo la consulta SQL.";

		return generateCompletion(prompt);
	}

	public String processSQLGeneration(String sqlQuery) {
		String prompt = "Eres un experto en SQL que verifica y corrige consultas SQL para una base de datos de tareas.\n" +
				"La tabla 'task' tiene los siguientes campos:\n" +
				"- id (INTEGER, clave primaria, autoincremental)\n" +
				"- description (TEXT)\n" +
				"- assigned_to (TEXT)\n" +
				"- status (TEXT, puede ser 'Pendiente', 'En Progreso', 'Bloqueada', o 'Completada')\n" +
				"- created_at (DATETIME, valor por defecto CURRENT_TIMESTAMP)\n\n" +
				"Consulta SQL a verificar: " + sqlQuery + "\n\n" +
				"Verifica que la consulta SQL sea válida y esté bien formada. Si es necesario, corrige la consulta.\n" +
				"Devuelve solo la consulta SQL corregida, sin explicaciones adicionales.";

		return generateCompletion(prompt);
	}

	private String generateCompletion(String promptText) {
		try {
			Prompt prompt = new Prompt(promptText);
			ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
			return response.getResult().getOutput().getContent();
		} catch (Exception e) {
			throw new RuntimeException("Error al procesar la solicitud: " + e.getMessage(), e);
		}
	}
}
