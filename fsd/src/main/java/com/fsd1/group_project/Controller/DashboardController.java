package com.fsd1.group_project.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class DashboardController {

    @GetMapping("/viewer")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<?> viewerDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Viewer Dashboard",
                "username", username,
                "role", "VIEWER"
        ));
    }

    @GetMapping("/task-manager")
    @PreAuthorize("hasRole('TASK_MANAGER')")
    public ResponseEntity<?> taskManagerDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Task Manager Dashboard",
                "username", username,
                "role", "TASK_MANAGER"
        ));
    }

    @GetMapping("/participant")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<?> participantDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Participant Dashboard",
                "username", username,
                "role", "PARTICIPANT"
        ));
    }
}