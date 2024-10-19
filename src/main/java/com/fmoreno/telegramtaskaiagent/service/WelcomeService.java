package com.fmoreno.telegramtaskaiagent.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class WelcomeService {

  private final TelegramClient telegramClient;

  public WelcomeService(TelegramClient telegramClient) {
    this.telegramClient = telegramClient;
  }

  public void showStartMessage(Long chatId) {
    String welcomeMessage =
        "¡Bienvenido! Soy el agente de IA Moreno.\n"
            + "Estoy aquí para ayudar a coordinar vuestras tareas.\n"
            + "Puedes decir, *Crea una tarea de hacer la compra* y todos podrán ver la tarea diciendo algo como: *Ver todas las tareas*.\n\n"
            + "Puedes asignarte o asignar tareas diciendo algo como *Asigname la tarea 6* o *Asigna la tarea 6 a Fernando* o *Asigna la tarea 6 a nadie*.\n\n"
            + "También puedes modificar el estado y decir, *Pon la tarea 3 en progreso* o *pon la tarea 3 en completada* o *pon la tarea 3 en pendiente*.\n\n"
            + "Esos son los 3 estados posibles.(Pendiente, En progreso, Completada)\n\n"
            + "También puedes decir *'Elimina la tarea 3'*, pero... cuidado con lo que eliminas...\n\n"
            + "Puedes empezar escribiendo *Ver todas las tareas* para ver todas las tareas disponibles.\n\n"
            + "Escribe *ayuda* para volver a ver este texto.\n\n"
            + "NOTA: No tengo memoria de chat, osea que no recuerdo el mensaje anterior. Solo reacciono al texto que se inserta nuevo.";
    sendMessage(chatId, welcomeMessage);
  }

  public void handleVerTodasLasTareas(Long chatId) {
    String message = "Ejecutando comando: ver todas las tareas";
    sendMessage(chatId, message);
    // Aquí puedes agregar la lógica para manejar la opción de ver todas las tareas
  }

  public void handleVerMisTareas(Long chatId) {
    String message = "Ejecutando comando: ver mis tareas";
    sendMessage(chatId, message);
    // Aquí puedes agregar la lógica para manejar la opción de ver mis tareas
  }

  public void showStartMessage(Long chatId, boolean isVerified) {
    String welcomeMessage;
    if (isVerified) {
      welcomeMessage =
          "¡Bienvenido! Estas son tus opciones:\n"
              + "/ver_todas_las_tareas - Ver todas las tareas\n"
              + "/ver_mis_tareas - Ver mis tareas";
    } else {
      welcomeMessage =
          "Hola! soy tu asistente de tareas, para verificar el acceso debes introducir tu correo electrónico";
    }
    sendMessage(chatId, welcomeMessage);
  }

  private void sendMessage(Long chatId, String text) {
    SendMessage message = SendMessage.builder().chatId(chatId).text(text).build();
    // replace special characters in MarkdownV2
    message.setText(message.getText().replace("!", "\\!"));
    message.setText(message.getText().replace(".", "\\."));
    message.setText(message.getText().replace("-", "\\-"));
    message.setText(message.getText().replace("(", "\\("));
    message.setText(message.getText().replace(")", "\\)"));

    message.setParseMode("MarkdownV2");
    try {
      telegramClient.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }
}
