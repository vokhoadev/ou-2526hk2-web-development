package com.ou.springcode.service;

import org.springframework.stereotype.Component;

import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.entity.User;

@Component
public class UserMapper {
    public UserReponse toReponse (User user) {
        if(user == null) return null;

        return new UserReponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getFullName(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
