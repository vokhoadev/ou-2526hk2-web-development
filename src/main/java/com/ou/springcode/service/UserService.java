package com.ou.springcode.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.dto.UserRequest;
import com.ou.springcode.entity.User;
import com.ou.springcode.dto.UserPatchRequest;
import com.ou.springcode.repository.UserRepository;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserReponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserReponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<UserReponse> findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return ResponseEntity.ok(UserReponse.fromEntity(user));
    }

    @Override
    public ResponseEntity<UserReponse> create(UserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserReponse.fromEntity(saved));
    }

    @Override
    public ResponseEntity<UserReponse> update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName() != null ? request.fullName() : user.getFullName());
        User updated = userRepository.save(user);
        return ResponseEntity.ok(UserReponse.fromEntity(updated));
    }

    @Override
    public ResponseEntity<UserReponse> patchUpdate(Long id, UserPatchRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (request.username() != null) user.setUsername(request.username());
        if (request.email() != null) user.setEmail(request.email());
        if (request.fullName() != null) user.setFullName(request.fullName());
        User updated = userRepository.save(user);
        return ResponseEntity.ok(UserReponse.fromEntity(updated));
    }

    @Override
    public boolean deleteById(Long id) {
        if (!userRepository.existsById(id))
            return false;
        userRepository.deleteById(id);
        return true;
    }
}
