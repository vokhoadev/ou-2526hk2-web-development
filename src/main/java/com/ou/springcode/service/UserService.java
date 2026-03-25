package com.ou.springcode.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.dto.UserRequest;
import com.ou.springcode.entity.Role;
import com.ou.springcode.entity.User;
import com.ou.springcode.exception.DuplicateResourceException;
import com.ou.springcode.repository.UserRepository;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserReponse> findAll(String search, Role role, Pageable pageable) {
        Page<User> page = userRepository.findAllSearchAndRole(search, role, pageable);
        return page.map(userMapper::toReponse);
    }

    @Transactional()
    public UserReponse create(UserRequest request) {
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
        log.info("User create: {} by admin", user.getUsername());
        return userMapper.toReponse(user);
    }
}
