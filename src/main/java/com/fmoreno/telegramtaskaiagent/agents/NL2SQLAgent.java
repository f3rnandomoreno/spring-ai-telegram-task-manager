package com.fmoreno.telegramtaskaiagent.agents;


import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
public class NL2SQLAgent {

    @Autowired
	private ChatClient chatClient;

	public String processNaturalLanguageToSQL(String input) {
		String prompt = "Eres un asistente que convierte instrucciones en lenguaje natural a consultas SQL para una base de datos de tareas.\n" +
				"La tabla 'tasks' tiene los siguientes campos:\n" +
				"- id (INTEGER, clave primaria, autoincremental)\n" +
				"- description (TEXT)\n" +
				"- assignee (TEXT)\n" +
				"- status (TEXT, puede ser 'TODO','BLOCKED','IN_PROGRESS','DONE')\n" +
				"- last_update_by_user (TEXT)\n" +
				"- created_at (DATETIME, valor por defecto CURRENT_TIMESTAMP)\n\n" +
				"- updated_at (DATETIME)\n\n" +
				"Instrucción del usuario: " + input + "\n\n" +
				"Genera una consulta SQL válida basada en esta instrucción. No incluyas explicaciones, solo la consulta SQL.";

		return extractSQLString(generateCompletion(prompt));
	}

	public String processSQLReview(String sqlQuery) {
		String prompt = "Eres un experto en SQL que verifica y corrige consultas SQL para una base de datos de tareas.\n" +
				"La tabla 'tasks' tiene los siguientes campos:\n" +
				"- id (INTEGER, clave primaria, autoincremental)\n" +
				"- description (TEXT)\n" +
				"- assignee (TEXT)\n" +
				"- status (TEXT, puede ser 'Pendiente', 'En Progreso', 'Bloqueada', o 'Completada')\n" +
				"- lastUpdateByUser (TEXT)\n" +
				"- created_at (DATETIME, valor por defecto CURRENT_TIMESTAMP)\n\n" +
				"- updated_at (DATETIME)\n\n" +
				"Consulta SQL a verificar: " + sqlQuery + "\n\n" +
				"Verifica que la consulta SQL sea válida y esté bien formada. Si es necesario, corrige la consulta.\n" +
				"Devuelve solo la consulta SQL corregida, sin explicaciones adicionales.";

		return extractSQLString(generateCompletion(prompt));
	}

	private String generateCompletion(String promptText) {
		log.info("Generating completion for prompt: {}", promptText);
		try {
			Prompt prompt = new Prompt(promptText);
			ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
			String result = response.getResult().getOutput().getContent();
			log.info("Generated completion: {}", result);
			return result;
		} catch (Exception e) {
			log.error("Error processing request", e);
			throw new RuntimeException("Error al procesar la solicitud: " + e.getMessage(), e);
		}
	}

	public String extractSQLString(String input) {
		String regex = "```sql\\s*(.*?)\\s*```";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}


}
