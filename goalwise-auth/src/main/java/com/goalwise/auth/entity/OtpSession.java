package com.goalwise.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "otp_sessions")
public class OtpSession {
    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Column(nullable = false)
    private UUID userId;

    @Setter
    @Column(nullable = false)
    private String vonageRequestId;

    @Setter
    private String purpose;    // SIGNUP | LOGIN | CONNECT_BANK
    @Setter
    private String channel;    // SMS | WHATSAPP
    @Setter
    private OtpStatus status = OtpStatus.PENDING; // PENDING | VERIFIED | FAILED | EXPIRED

    @Setter
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public OtpSession() {}

    public OtpSession(UUID userId, String vonageRequestId, String purpose, String channel) {
        this.userId = userId;
        this.vonageRequestId = vonageRequestId;
        this.purpose = purpose;
        this.channel = channel;
    }

}
