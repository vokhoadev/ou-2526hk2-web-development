package com.ou.springcode.dto;

import java.time.Instant;

import com.ou.springcode.entity.Role;

public record UserReponse(
    Long id,
    String username,
    String email,
    Role role,
    String fullName,
    Instant createdAt,
    Instant updatedAt
) {}
