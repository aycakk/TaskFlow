package com.example.taskflow.application.dto;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskCreateRequest {

    @NotNull
    private Long userId;

    @NotBlank(message = "boş olamaz")
    @Size(max = 120)
    private String title;


    @Size(max = 2000)
    private String explain;



    private Long startDate;
    private Long endDate;

}
