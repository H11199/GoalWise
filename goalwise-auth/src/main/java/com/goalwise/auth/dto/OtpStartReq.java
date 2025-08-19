package com.goalwise.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpStartReq(
    String email,         // optional if already authenticated; else required
    @NotBlank String purpose, // SIGNUP | LOGIN | CONNECT_BANK
    String channel        // SMS | WHATSAPP
) {}
