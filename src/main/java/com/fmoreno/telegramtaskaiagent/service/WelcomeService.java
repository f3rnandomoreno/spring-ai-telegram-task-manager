package com.fmoreno.telegramtaskaiagent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WelcomeService {

  private final MessageService messageService;

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
    messageService.sendMessage(chatId, welcomeMessage);
  }

}
