package com.ou.springcode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ou.springcode.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
