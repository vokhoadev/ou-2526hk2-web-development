package com.ou.springcode.dto;

import java.time.LocalDateTime;

import com.ou.springcode.model.User;

public record UserReponse(
    Long id,
    String username,
    String email,
    String fullName,
    LocalDateTime createdAt
) {
    public static UserReponse fromEntity(User user) {
        return new UserReponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getCreatedAt()
        );
    }
}
