package com.goalwise.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Setter
    @Column(unique = true, nullable = false)
    private String phone; // E.164 format e.g. +9199xxxxxxx

    @Setter
    private boolean phoneVerified = false;

    @Setter
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public User() {}

    public User(String email, String passwordHash, String phone) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
    }

}
