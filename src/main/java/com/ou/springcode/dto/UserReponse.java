package com.ou.springcode.dto;

import java.time.LocalDateTime;

import com.ou.springcode.entity.Role;

public record UserReponse(
    Long id,
    String username,
    String email,
    Role role,
    String fullName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
