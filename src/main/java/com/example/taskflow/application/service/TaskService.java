package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.TaskCreateRequest;
import com.example.taskflow.application.dto.TaskResponse;
import com.example.taskflow.application.dto.TaskUpdateRequest;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import com.example.taskflow.infrastructure.persistence.repository.TaskRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listByUserPaged(Long userId,
                                              boolean includeDeleted,
                                              int page,
                                              int size) {

        PageRequest pageable = PageRequest.of(page, size);

        Page<TaskEntity> entityPage = includeDeleted
                ? taskRepository.findAllByUserId(userId, pageable)
                : taskRepository.findActiveTasks(userId, pageable);

        return entityPage.map(this::toResponse);
    }

    // ✅ DEĞİŞTİ: userId artık request'ten değil, parametreden geliyor
    public TaskResponse create(Long userId, TaskCreateRequest request) {
        TaskEntity entity = new TaskEntity();

        entity.setUserId(userId);
        entity.setTitle(request.getTitle() == null ? "" : request.getTitle());
        entity.setExplain(request.getExplain() == null ? "" : request.getExplain());

        entity.setStartDate(request.getStartDate() == null ? 0L : request.getStartDate());
        entity.setEndDate(request.getEndDate() == null ? 0L : request.getEndDate());

        // ✅ client göndermese de backend default
        entity.setIsCompleted(false);
        entity.setIsDeleted(false);

        // Şu an sen hep "now" basıyorsun:
        entity.setDate(System.currentTimeMillis());

        TaskEntity saved = taskRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listByUser(Long userId, boolean includeDeleted) {
        List<TaskEntity> list = includeDeleted
                ? taskRepository.findAllByUserId(userId)
                : taskRepository.findActiveTasks(userId);

        return list.stream().map(this::toResponse).toList();
    }

    public TaskResponse update(Long userId, String taskId, TaskUpdateRequest request) {
        TaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // OWNERSHIP CHECK
        if (entity.getUserId() == null || !entity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu task sana ait değil.");
        }

        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getExplain() != null) entity.setExplain(request.getExplain());
        if (request.getIsCompleted() != null) entity.setIsCompleted(request.getIsCompleted());
        if (request.getStartDate() != null) entity.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) entity.setEndDate(request.getEndDate());
        if (request.getDate() != null) entity.setDate(request.getDate());

        if (request.getVersion() != null) {
            entity.setVersion(request.getVersion());
        }

        TaskEntity saved = taskRepository.save(entity);
        return toResponse(saved);
    }


    public void softDelete(Long userId, String taskId) {
        TaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));


        if (entity.getUserId() == null || !entity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu task sana ait değil.");
        }

        entity.setIsDeleted(true);
        taskRepository.save(entity);
    }


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

    @Transactional(readOnly = true)
    public TaskResponse getById(Long userId, String taskId) {
        TaskEntity entity = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // ✅ OWNERSHIP CHECK
        if (entity.getUserId() == null || !entity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu task sana ait değil.");
        }

        // Soft delete edilmişse göstermeyelim (istersen bunu opsiyonlu yaparız)
        if (Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + taskId);
        }

        return toResponse(entity);
    }

}
