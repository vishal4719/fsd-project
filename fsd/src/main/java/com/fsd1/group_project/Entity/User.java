package com.fsd1.group_project.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    private String name;
    private String clg_name;

    @Column(unique = true)
    private String email;
    private String resetToken;
    private LocalDateTime tokenCreationDate;

    @Column(unique = true)
    private String phone_no;

    private LocalDateTime date;

    private String password;

    @Column(name = "roles") // Store roles as a comma-separated string
    private String roles;

}