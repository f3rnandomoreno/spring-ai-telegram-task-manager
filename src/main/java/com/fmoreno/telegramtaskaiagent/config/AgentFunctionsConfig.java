package com.fmoreno.telegramtaskaiagent.config;

import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
@Log4j2
public class AgentFunctionsConfig {

    @Bean
    @Description("Crea una nueva tarea con la descripción y asignación proporcionadas")
    public Function<TaskService.Request, TaskService.Response> crearTarea(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.CREAR, request.taskId(), request.description(), request.assignee()));
    }

    @Bean
    @Description("Modifica una tarea existente con los nuevos detalles proporcionados")
    public Function<TaskService.Request, TaskService.Response> modificarTarea(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.MODIFICAR, request.taskId(), request.description(), request.assignee()));
    }

    @Bean
    @Description("Elimina una tarea existente por su ID")
    public Function<TaskService.Request, TaskService.Response> eliminarTarea(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.ELIMINAR, request.taskId(), request.description(), request.assignee()));
    }

    @Bean
    @Description("Muestra todas las tareas o filtra por criterios específicos")
    public Function<TaskService.Request, TaskService.Response> verTareas(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.VER, request.taskId(), request.description(), request.assignee()));
    }
}