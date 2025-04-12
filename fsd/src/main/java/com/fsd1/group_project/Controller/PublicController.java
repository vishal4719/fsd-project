package com.fsd1.group_project.Controller;
import com.fsd1.group_project.Entity.LoginRequest;
import com.fsd1.group_project.Entity.User;
import com.fsd1.group_project.Service.JwtBlacklistService;
import com.fsd1.group_project.Service.UserService;
import com.fsd1.group_project.Utils.JwtUtils;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
import java.util.UUID;

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
    @Autowired
    private JwtBlacklistService jwtBlacklistService;
    @Autowired
    private JavaMailSender mailSender;


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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid or missing token"));
        }

        String token = authorizationHeader.substring(7); // Extract the token
        jwtBlacklistService.blacklistToken(token); // Add the token to the blacklist

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            Optional<User> user = userService.findByEmail(email);
            if (user.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "If your email is registered, you will receive a password reset link"));
            }

            // Check if there's an existing valid token
            User u = user.get();
            if (u.getResetToken() != null &&
                    u.getTokenCreationDate() != null &&
                    u.getTokenCreationDate().plusMinutes(7).isAfter(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "A reset link has already been sent. Please wait before requesting another."));
            }

            // Generate reset token
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(u, token);

            // Prepare email content
            String resetUrl = "http://localhost:4200/reset-password?token=" + token;

            String emailContent = "<html><body>" +
                    "<h2>Password Reset Request</h2>" +
                    "<p>Hello " + u.getName() + ",</p>" +
                    "<p>You have requested to reset your password. Click the link below to set a new password:</p>" +
                    "<p><a href='" + resetUrl + "'>Reset Password</a></p>" +
                    "<p>This link will expire in 7 minutes.</p>" +
                    "<p>If you didn't request this, please ignore this email.</p>" +
                    "</body></html>";

            try {
                // Send email
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom("vigomi024@gmail.com");
                helper.setTo(email);
                helper.setSubject("Password Reset Request");
                helper.setText(emailContent, true);

                mailSender.send(message);
                return ResponseEntity.ok(Map.of("message", "If your email is registered, you will receive a password reset link"));
            } catch (Exception mailEx) {
                // Log the email error but don't expose it to the user
                System.err.println("Failed to send email: " + mailEx.getMessage());
                return ResponseEntity.ok(Map.of("message", "If your email is registered, you will receive a password reset link"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process request. Please try again later."));
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            if (newPassword == null || newPassword.trim().length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password must be at least 8 characters long"));
            }
            String validationResult = userService.validatePasswordResetToken(token);
            if (validationResult != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", validationResult));
            }
            User user = userService.getUserByPasswordResetToken(token);
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid token"));
            }

            userService.changeUserPassword(user, newPassword);
            return ResponseEntity.ok()
                    .body(Map.of("message", "Password has been reset successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset password. Please try again later."));
        }
    }




}