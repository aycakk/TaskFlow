package com.example.taskflow.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    @Id
    @Column(name = "task_id", nullable = false, length = 36)
    private String id;

    @Column(name = "remote_id")
    private Integer remoteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 120)
    private String title = "";

    @Column(name = "explain", length = 2000)
    private String explain = "";

    @Column(name = "is_complete", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "start_time", nullable = false)
    private Long startDate = 0L;

    @Column(name = "end_time", nullable = false)
    private Long endDate = 0L;

    @Column(name = "date", nullable = false)
    private Long date;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "created_time", nullable = false, updatable = false)
    private Long createdTime;

    @Column(name = "updated_time", nullable = false)
    private Long updatedTime;

    @Column(name = "dummy_field")
    private String dummyField;

    @PrePersist
    void prePersist() {
        long now = System.currentTimeMillis();

        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.date == null) this.date = now;
        if (this.createdTime == null) this.createdTime = now;
        if (this.updatedTime == null) this.updatedTime = now;

        if (this.version == null) this.version = 1;
        if (this.isCompleted == null) this.isCompleted = false;
        // isDeleted nullable kalsın (mobilde de nullable)
    }

    @PreUpdate
    void preUpdate() {
        this.updatedTime = System.currentTimeMillis();
    }
}
