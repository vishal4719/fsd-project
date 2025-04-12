package com.fsd1.group_project.Service;

import com.fsd1.group_project.Entity.User;
import com.fsd1.group_project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
   @Autowired
    private PasswordEncoder passwordEncoder;
    public void saveNewUser(User user) {
        userRepository.save(user);
    }


    public List<User>getAll(){
       return  userRepository.findAll();
}
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public User getUserByPasswordResetToken(String token) {
        return userRepository.findByResetToken(token);
    }
    public void createPasswordResetTokenForUser(User user, String token) {
        // Invalidate any existing token
        user.setResetToken(token);
        user.setTokenCreationDate(LocalDateTime.now());
        userRepository.save(user);
    }

    public String validatePasswordResetToken(String token) {
        User user = userRepository.findByResetToken(token);

        if (user == null) {
            return "Invalid token";
        }

        if (user.getTokenCreationDate() == null) {
            return "Invalid token";
        }

        LocalDateTime tokenCreationDate = user.getTokenCreationDate();
        if (tokenCreationDate.plusMinutes(7).isBefore(LocalDateTime.now())) {
            // Clear expired token
            user.setResetToken(null);
            user.setTokenCreationDate(null);
            userRepository.save(user);
            return "Token has expired";
        }

        return null;
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        // Invalidate the token after successful password change
        user.setResetToken(null);
        user.setTokenCreationDate(null);
        userRepository.save(user);
    }



}