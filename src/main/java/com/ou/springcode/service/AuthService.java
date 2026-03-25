package com.ou.springcode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer.JwtConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ou.springcode.config.JwtProperties;
import com.ou.springcode.dto.AuthReponse;
import com.ou.springcode.dto.LoginRequest;
import com.ou.springcode.dto.RegisterRequest;
import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.entity.Role;
import com.ou.springcode.entity.User;
import com.ou.springcode.exception.DuplicateResourceException;
import com.ou.springcode.exception.ResourceNotFoundException;
import com.ou.springcode.repository.UserRepository;
import com.ou.springcode.security.JwtService;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService,
        UserMapper userMapper,
        JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userMapper = userMapper;
        this.jwtProperties = jwtProperties;
    }

    // register
    @Transactional
    public UserReponse register(RegisterRequest request) {

        if(userRepository.existsByUsername(request.username())){
            throw new DuplicateResourceException("Username đã tồn tại");
        }
        if(userRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("Email đã tồn tại");
        }

        User user = User.builder()
                    .username(request.username())
                    .email(request.email())
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .role(Role.USER)
                    .build();
        user = userRepository.save(user);
        return userMapper.toReponse(user);
    }
    // login
    public AuthReponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.usernameOrEmail())
                    .or(() -> userRepository.findByEmail(request.usernameOrEmail()))
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
        );

        var userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        long expiresIn = jwtProperties.getAccessTokenExpiresMs() / 1000;

        return AuthReponse.of(
            accessToken, 
            refreshToken, "Bearer", 
            expiresIn, 
            userMapper.toReponse(user)
        );
    }

    // getCurrentUser
    public UserReponse getCurrentUser(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Chưa đăng nhập");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User",username));
        return userMapper.toReponse(user);
    }
}
