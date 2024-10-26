package com.fmoreno.telegramtaskaiagent.agents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.stringtemplate.v4.ST;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Log4j2
@Service
public class NL2SQLAgent {

  private final ChatClient chatClient;
  private final ResourceLoader resourceLoader;
  private final static String NL2SQL_PROMPT_TEMPLATE = "classpath:prompts/nl2sql_prompt.st";

  public NL2SQLAgent(ChatClient chatClient, ResourceLoader resourceLoader) {
    this.chatClient = chatClient;
    this.resourceLoader = resourceLoader;
  }

  public String processNaturalLanguageToSQL(String input, String assignee) {
    String templateContent = loadPromptTemplate(NL2SQL_PROMPT_TEMPLATE);
    ST st = new ST(templateContent);
    st.add("input", input);
    st.add("assignee", assignee);
    String prompt = st.render();

    return extractSQLString(generateCompletion(prompt));
  }

  private String loadPromptTemplate(String resourcePath) {
    try {
      Resource resource = resourceLoader.getResource(resourcePath);
      return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Error loading prompt template", e);
      throw new RuntimeException("Error loading prompt template: " + e.getMessage(), e);
    }
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
