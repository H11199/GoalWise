package com.goalwise.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyReq(
    @NotBlank String requestId,
    @NotBlank String code
) {}
