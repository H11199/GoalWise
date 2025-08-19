package com.goalwise.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginReq(
    @Email @NotBlank String email,
    @NotBlank String password
) {}
