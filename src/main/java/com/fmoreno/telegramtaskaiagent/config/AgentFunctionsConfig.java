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
    @Description("Creates a new task with the provided description and assignment")
    public Function<TaskService.Request, TaskService.Response> createTask(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.CREATE, request.taskId(), request.description(), request.assignee()));
    }

    @Bean
    @Description("Modifies an existing task with the new details provided")
    public Function<TaskService.Request, TaskService.Response> updateTask(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.UPDATE, request.taskId(), request.description(), request.assignee()));
    }

    @Bean
    @Description("Deletes an existing task by its ID")
    public Function<TaskService.Request, TaskService.Response> deleteTask(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.DELETE, request.taskId(), request.description(), request.assignee()));
    }

    @Bean
    @Description("Displays all tasks or filters by specific criteria")
    public Function<TaskService.Request, TaskService.Response> viewTasks(TaskService taskService) {
        return request -> taskService.apply(new TaskService.Request(TaskService.Action.VIEW, request.taskId(), request.description(), request.assignee()));
    }
}