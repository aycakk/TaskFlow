package com.example.taskflow.presentation.controller;

import com.example.taskflow.application.dto.TaskCreateRequest;
import com.example.taskflow.application.dto.TaskResponse;
import com.example.taskflow.application.dto.TaskUpdateRequest;
import com.example.taskflow.application.service.TaskService;
import com.example.taskflow.infrastructure.persistence.entity.UserEntity;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //  Token’dan userId çek
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        //  principal = UserEntity
        if (principal instanceof UserEntity user) {
            return user.getId();
        }

        // Eğer principal String geliyorsa (username), burada şimdilik patlatalım ki fark edelim
        throw new IllegalStateException("JWT principal UserEntity değil. JwtAuthenticationFilter içinde principal olarak UserDetails/UserEntity set etmelisin.");
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest request) {
        Long userId = currentUserId();
        return taskService.create(userId, request);
    }

    // LIST (artık userId parametresi yok)
    // örnek: GET /api/tasks?includeDeleted=false
    @GetMapping
    public List<TaskResponse> list(
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        Long userId = currentUserId();
        return taskService.listByUser(userId, includeDeleted);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable String id) {
        Long userId = currentUserId();
        return taskService.getById(userId, id);
    }


    @GetMapping("/paged")
    public Page<TaskResponse> listPaged(
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = currentUserId();
        return taskService.listByUserPaged(userId, includeDeleted, page, size);
    }

    // UPDATE (patch gibi çalışıyor)
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable String id,
                               @Valid @RequestBody TaskUpdateRequest request) {
        Long userId = currentUserId();
        return taskService.update(userId,id, request);
    }

    @PatchMapping("/{id}")
    public TaskResponse patchUpdate(@PathVariable String id,
                                    @RequestBody TaskUpdateRequest request) {
        Long userId = currentUserId();
        return taskService.update(userId,id, request);
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        Long userId = currentUserId();
        taskService.softDelete(userId,id);
    }
}
