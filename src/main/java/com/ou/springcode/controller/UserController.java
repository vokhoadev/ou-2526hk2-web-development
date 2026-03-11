package com.ou.springcode.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ou.springcode.dto.UserReponse;
import com.ou.springcode.dto.UserRequest;
import com.ou.springcode.dto.UserPatchRequest;
import com.ou.springcode.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users - Lấy danh sách tất cả user */
    @GetMapping
    public ResponseEntity<List<UserReponse>> getAllUsers() {
        List<UserReponse> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    /** GET /api/users/{id} - Lấy user theo id */
    @GetMapping("/{id}")
    public ResponseEntity<UserReponse> getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    /** POST /api/users - Tạo user mới */
    @PostMapping
    public ResponseEntity<UserReponse> create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    /** PUT /api/users/{id} - Cập nhật toàn bộ user */
    @PutMapping("/{id}")
    public ResponseEntity<UserReponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    /** PATCH /api/users/{id} - Cập nhật một phần: chỉ gửi field cần đổi (username, email, fullName đều optional) */
    @PatchMapping("/{id}")
    public ResponseEntity<UserReponse> patchUpdate(@PathVariable Long id, @Valid @RequestBody UserPatchRequest request) {
        return userService.patchUpdate(id, request);
    }

    /** DELETE /api/users/{id} - Xóa user */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
