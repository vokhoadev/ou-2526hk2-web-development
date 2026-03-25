package com.ou.springcode.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ou.springcode.dto.AuthReponse;
import com.ou.springcode.dto.LoginRequest;
import com.ou.springcode.dto.RegisterRequest;
import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserReponse> register(@Valid @RequestBody RegisterRequest request){
        UserReponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthReponse> login(@Valid @RequestBody LoginRequest request){
        AuthReponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserReponse> me(Authentication authentication){
        UserReponse user = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(user);
    }
}
