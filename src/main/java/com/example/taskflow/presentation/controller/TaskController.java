package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.dto.TaskCreateRequest;
import com.example.taskflow.application.dto.TaskResponse;
import com.example.taskflow.application.dto.TaskUpdateRequest;
import com.example.taskflow.application.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.create(request);
    }

    // LIST
    // örnek: GET /api/tasks?userId=1&includeDeleted=false
    @GetMapping
    public List<TaskResponse> list(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return taskService.listByUser(userId, includeDeleted);
    }

    // UPDATE (patch gibi çalışıyor)
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable String id,
                               @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.update(id, request);
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        taskService.softDelete(id);
    }
}
