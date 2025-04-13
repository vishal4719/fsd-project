package com.fsd1.group_project.Service;

import com.fsd1.group_project.Entity.User;
import com.fsd1.group_project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("User not found with email: " + email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Get the role and ensure it's one of the valid roles
        String role = user.getRoles();
        if (role == null || role.trim().isEmpty()) {
            role = "USER"; // Default role
        }
        
        // Validate role
        if (!role.equals("USER") && !role.equals("TASK_MANAGER")) {
            role = "USER"; // Default to USER if invalid role
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(role) // Pass the single role
                .build();
    }
}