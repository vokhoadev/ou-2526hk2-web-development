package com.ou.springcode.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50)
    String username,

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email,

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password tối thiểu 8 ký tự")
    String password
) {}
