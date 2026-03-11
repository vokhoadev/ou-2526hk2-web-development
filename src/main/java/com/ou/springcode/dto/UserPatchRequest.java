package com.ou.springcode.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO cho PATCH: tất cả field đều optional, chỉ cập nhật field được gửi.
 */
public record UserPatchRequest(
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    String username,

    @Email(message = "Email không hợp lệ")
    String email,

    @Size(max = 100)
    String fullName
) {}
