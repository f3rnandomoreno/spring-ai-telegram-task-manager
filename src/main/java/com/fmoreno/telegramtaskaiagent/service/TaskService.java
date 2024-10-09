package com.fmoreno.telegramtaskaiagent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class TaskService /*implements Function<TaskService.Request, TaskService.Response>*/ {


    final JdbcTemplate jdbcTemplate;

    public void executeSQLQuery(String sqlQuery) {
        log.info("Executing SQL Query: {}", sqlQuery);
        jdbcTemplate.execute(sqlQuery);
    }

   /* public enum Action { UPDATE, DELETE, CREATE, VIEW }
    public record Request(Action action, String taskId, String description, String assignee) {}
    public record Response(String message, boolean success) {}*/

    /*@Override
    public Response apply(Request request) {
        return switch (request.action()) {
            case UPDATE -> updateTask(request);
            case DELETE -> deleteTask(request);
            case CREATE -> createTask(request);
            case VIEW -> viewTasks(request);
            default -> new Response("Invalid action.", false);
        };
    }

    private Response updateTask(Request request) {
        log.info("Updating task: {}", request);
        return new Response("Task updated successfully.", true);
    }

    private Response deleteTask(Request request) {
        log.info("Deleting task: {}", request);
        return new Response("Task deleted successfully.", true);
    }

    private Response createTask(Request request) {
        log.info("Creating task: {}", request);
        return new Response("Task created successfully.", true);
    }

    private Response viewTasks(Request request) {
        log.trace("Viewing tasks: {}", request);
        return new Response("Tasks viewed successfully.", true);
    }*/
}