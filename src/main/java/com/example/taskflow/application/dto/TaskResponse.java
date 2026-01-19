package com.example.taskflow.application.dto;


import lombok.Data;

@Data
public class TaskResponse {

    private String id;
    private Integer remoteId;
    private Long userId;

    private String title;
    private String explain;

    private Boolean isCompleted;
    private Boolean isDeleted;

    private Long startDate;
    private Long endDate;
    private Long date;

    private Integer version;
    private Long createdTime;
    private Long updatedTime;

    private String dummyField;
}

