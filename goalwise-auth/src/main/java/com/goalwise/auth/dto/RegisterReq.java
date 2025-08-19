package com.goalwise.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterReq(
    @Email @NotBlank String email,
    @Size(min=8) String password,
    @NotBlank 
    String phone // E.164
) {}
