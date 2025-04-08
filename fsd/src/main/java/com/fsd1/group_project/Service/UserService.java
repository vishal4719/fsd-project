package com.fsd1.group_project.Service;

import com.fsd1.group_project.Entity.User;
import com.fsd1.group_project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        // Password is already encoded in UserController
        user.setRoles(String.valueOf(Arrays.asList("USER")));
        userRepository.save(user);
    }

    public List<User>getAll(){
       return  userRepository.findAll();
}
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }



}