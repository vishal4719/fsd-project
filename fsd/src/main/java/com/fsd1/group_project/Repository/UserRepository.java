// repository/UserRepository.java
package com.fsd1.group_project.Repository;


import com.fsd1.group_project.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    // Add this to check if an email exists
    boolean existsByEmail(String email);
}
