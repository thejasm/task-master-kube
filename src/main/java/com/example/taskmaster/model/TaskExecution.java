package com.example.taskmaster.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
public class TaskExecution {
    private Date startTime;
    private Date endTime;
    private String output;
}