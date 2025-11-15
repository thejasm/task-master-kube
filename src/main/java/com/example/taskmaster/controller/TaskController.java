package com.example.taskmaster.controller;

import com.example.taskmaster.model.Task;
import com.example.taskmaster.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private TaskService svc;

    public TaskController(TaskService svc) { this.svc = svc; }

    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String id) {
        if (id != null) return ResponseEntity.ok(svc.retrieve(id));
        return ResponseEntity.ok(svc.all());
    }

    @PutMapping
    public Task putTask(@RequestBody Task task) { return svc.save(task); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        svc.del(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find")
    public ResponseEntity<?> findTasksByName(@RequestParam String name) {
        return ResponseEntity.ok(svc.byName(name));
    }

    @PutMapping("/{id}/execute")
    public Task executeTask(@PathVariable String id) { return svc.exec(id); }
}