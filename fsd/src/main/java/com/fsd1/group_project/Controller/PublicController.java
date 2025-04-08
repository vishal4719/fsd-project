package com.fsd1.group_project.Controller;
import com.fsd1.group_project.Entity.LoginRequest;
import com.fsd1.group_project.Entity.User;
import com.fsd1.group_project.Service.UserService;
import com.fsd1.group_project.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class PublicController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "email is required"));

            }
            Optional<User> existingUser = userService.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "email already exists"));
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error","name is required"));

            }
            if (user.getClg_name() == null || user.getClg_name().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "College name is required"));

            }
            if (user.getPhone_no() == null||user.getPhone_no().length()<10||user.getPhone_no().length()>10) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone no should be of 10 digits"+user.getPhone_no()+"length is: "+user.getPhone_no().length()));

            }
            user.setDate(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.saveNewUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message","User Registered Succesfully"));
        }catch (Exception e){
            e.printStackTrace();

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for email: " + loginRequest.getEmail());

            // Check if user exists first
            Optional<User> optionalUser = userService.findByEmail(loginRequest.getEmail());
            if (optionalUser.isEmpty()) {
                System.out.println("User not found with email: " + loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception authEx) {
                System.out.println("Authentication failed: " + authEx.getMessage());
                authEx.printStackTrace();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication failed: " + authEx.getMessage()));
            }
            String token = jwtUtils.generateToken(loginRequest.getEmail());
            User user = optionalUser.get();
            String role = user.getRoles();

            System.out.println("Login successful for email: " + loginRequest.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token,
                    "username", loginRequest.getEmail(),
                    "role", role
            ));
        } catch (Exception e) {
            System.err.println("Login failed for email: " + loginRequest.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password: " + e.getMessage()));
        }
    }
       @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
    
        Optional<User> optionalUser = userService.findByEmail(currentUserEmail);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
    
        User user = optionalUser.get();
    
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to your dashboard",
                "username", user.getName(),
                "email", user.getEmail(),
                "roles", user.getRoles(),
                "clg_name", user.getClg_name()
        ));
    }



}