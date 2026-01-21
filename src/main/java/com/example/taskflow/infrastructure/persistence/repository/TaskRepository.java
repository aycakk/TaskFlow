package com.example.taskflow.infrastructure.persistence.repository;

import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {
    Page<TaskEntity> findAllByUserId(Long userId, Pageable pageable);

    @Query("select t from TaskEntity t where t.userId = :userId and (t.isDeleted = false)")
    Page<TaskEntity> findActiveTasks(@Param("userId") Long userId, Pageable pageable);

    List<TaskEntity> findAllByUserId(Long userId);

    List<TaskEntity> findAllByUserIdAndIsDeletedFalse(Long userId);
    @Query("select t from TaskEntity t where t.userId = :userId and (t.isDeleted = false or t.isDeleted is null)")
    List<TaskEntity> findActiveTasks(@Param("userId") Long userId);


}