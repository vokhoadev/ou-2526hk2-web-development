package com.ou.springcode.repository;

import java.util.Optional;

import com.ou.springcode.model.User;

public interface IUserRepository {
    java.util.List<User> findAll();
    User save(User user);
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<User> findById(Long id);
}
