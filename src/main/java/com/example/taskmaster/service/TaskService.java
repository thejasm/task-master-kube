package com.example.taskmaster.service;

import com.example.taskmaster.model.Task;
import com.example.taskmaster.model.TaskExecution;
import com.example.taskmaster.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class TaskService {

    private TaskRepository repo;
    private KubeService kubeSvc;
    private ValidationService valSvc;

    public TaskService(TaskRepository repo, KubeService kubeSvc, ValidationService valSvc) {
        this.repo = repo;
        this.kubeSvc = kubeSvc;
        this.valSvc = valSvc;
    }

    public List<Task> all() { return repo.findAll(); }

    @SuppressWarnings("null")
    public Task retrieve(String id) { return repo.findById(id).get(); }

    public List<Task> byName(String name) {
        List<Task> tasks = repo.findByNameContainingIgnoreCase(name);
        
        if (tasks.isEmpty()) System.err.println("No tasks found!");

        return tasks;
    }

    //@SuppressWarnings("null")
    public Task save(Task task) { 
        String valid;
        valid = valSvc.validate(task.getCommand());

        if (valid == null) return repo.save(task);
        else System.err.println("The request contains a banned word: " + valid);
        return null;
    }

    @SuppressWarnings("null")
    public void del(String id) {
        Task task = retrieve(id);
        repo.delete(task);
    }

    public Task exec(String id) {
        Task task = retrieve(id);
        TaskExecution execObj = new TaskExecution();
        execObj.setStartTime(new Date());

        try {
            String out = kubeSvc.run(task.getCommand());
            execObj.setOutput(out);
        } catch (Exception e) {
            execObj.setOutput("Failed: " + e.getMessage());
        }
        
        execObj.setEndTime(new Date());
        task.getTaskExecutions().add(execObj);
        return repo.save(task);
    }
}