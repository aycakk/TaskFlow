package com.example.taskflow.infrastructure.persistence.repository;

import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {

    List<TaskEntity> findAllByUserId(Long userId);

    List<TaskEntity> findAllByUserIdAndIsDeletedFalse(Long userId);
}