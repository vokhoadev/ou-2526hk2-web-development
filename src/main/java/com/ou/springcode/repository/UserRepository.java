package com.ou.springcode.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ou.springcode.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername (String username);

    Optional<User> findByEmail(String email);

    // Query derivation : Cơ chế phát sinh truy vấn từ tên method
    // Dành cho query đơn giản, không có @Query 
    // findByUser 
    // Cơ chế : Spring Data JPA đọc tên method, suy ra điều kiện truy vấn >> tự sinh SQL
    // Quy ước tên: 
    // findBy... -> SELECT ..., điều kiện theo thuộc tính sau By 
    // existsBy ... -> Kiểm tra tồn tại (COUNT / EXISTS))
    // AndIdNot -> thêm điều kiện: AND id != :id
    // Không viết lệnh truy vấn, chỉ cần đặt tên theo đúng quy tắc => JPA sẽ imlement giúp

    Optional<User> existsByUsername(String email);

    Optional<User> existsByEmail(String email);

}
