package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.TaskCreateRequest;
import com.example.taskflow.application.dto.TaskResponse;
import com.example.taskflow.application.dto.TaskUpdateRequest;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import com.example.taskflow.infrastructure.persistence.repository.TaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse create(TaskCreateRequest request) {
        TaskEntity entity = new TaskEntity();
        entity.setUserId(request.getUserId());
        entity.setTitle(request.getTitle() == null ? "" : request.getTitle());
        entity.setExplain(request.getExplain() == null ? "" : request.getExplain());

        entity.setIsCompleted(request.getIsCompleted() != null && request.getIsCompleted());
        entity.setIsDeleted(request.getIsDeleted()); // nullable kalsın

        entity.setStartDate(request.getStartDate() == null ? 0L : request.getStartDate());
        entity.setEndDate(request.getEndDate() == null ? 0L : request.getEndDate());

        // date null gelirse entity @PrePersist now basacak;
        entity.setDate(request.getDate());

        // version/createdTime/updatedTime/id -> @PrePersist
        TaskEntity saved = taskRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listByUser(Long userId, boolean includeDeleted) {
        List<TaskEntity> list = includeDeleted
                ? taskRepository.findAllByUserId(userId)
                : taskRepository.findAllByUserIdAndIsDeletedFalse(userId);

        return list.stream().map(this::toResponse).toList();
    }

    public TaskResponse update(String taskId, TaskUpdateRequest request) {
        TaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // Patch update: null geleni değiştirmiyoruz
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getExplain() != null) entity.setExplain(request.getExplain());

        if (request.getIsCompleted() != null) entity.setIsCompleted(request.getIsCompleted());
        if (request.getIsDeleted() != null) entity.setIsDeleted(request.getIsDeleted());

        if (request.getStartDate() != null) entity.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) entity.setEndDate(request.getEndDate());
        if (request.getDate() != null) entity.setDate(request.getDate());

        //  version otomatik artmıyor.
        // Eğer client version gönderiyorsa ve
        if (request.getVersion() != null) {
            entity.setVersion(request.getVersion());
        }

        TaskEntity saved = taskRepository.save(entity); // @PreUpdate updatedTime günceller
        return toResponse(saved);
    }

    public void softDelete(String taskId) {
        TaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        entity.setIsDeleted(true);
        taskRepository.save(entity);
    }

    // ---- Mapper ----
    private TaskResponse toResponse(TaskEntity e) {
        TaskResponse r = new TaskResponse();
        r.setId(e.getId());
        r.setRemoteId(e.getRemoteId());
        r.setUserId(e.getUserId());
        r.setTitle(e.getTitle());
        r.setExplain(e.getExplain());
        r.setIsCompleted(e.getIsCompleted());
        r.setIsDeleted(e.getIsDeleted());
        r.setStartDate(e.getStartDate());
        r.setEndDate(e.getEndDate());
        r.setDate(e.getDate());
        r.setVersion(e.getVersion());
        r.setCreatedTime(e.getCreatedTime());
        r.setUpdatedTime(e.getUpdatedTime());
        r.setDummyField(e.getDummyField());
        return r;
    }
}
