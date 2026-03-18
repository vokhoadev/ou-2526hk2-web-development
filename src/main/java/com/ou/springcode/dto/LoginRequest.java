package com.ou.springcode.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank(message = "Username hoặc email không được để trống")
    String usernameOrEmail,

    @NotBlank(message = "Password không được để trống")
    String password
) {}
