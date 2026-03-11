package com.ou.springcode.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.dto.UserRequest;

public interface IUserService {
    List<UserReponse> findAll();
    ResponseEntity<UserReponse> findById(Long id);
    ResponseEntity<UserReponse> create(UserRequest request);
    ResponseEntity<UserReponse> update(Long id, UserRequest request);
    boolean deleteById(Long id);
}
