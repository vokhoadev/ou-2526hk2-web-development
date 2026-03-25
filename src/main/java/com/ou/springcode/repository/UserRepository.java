package com.ou.springcode.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ou.springcode.entity.Role;
import com.ou.springcode.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername (String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:search, '%'))) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<User> findAllSearchAndRole(
        @Param("search") String search, 
        @Param("role") Role role,
        Pageable pageable);
}
