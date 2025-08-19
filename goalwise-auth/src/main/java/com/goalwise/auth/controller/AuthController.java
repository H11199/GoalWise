package com.goalwise.auth.controller;

import com.goalwise.auth.dto.*;
import com.goalwise.auth.entity.OtpSession;
import com.goalwise.auth.entity.User;
import com.goalwise.auth.repo.OtpSessionRepository;
import com.goalwise.auth.repo.UserRepository;
import com.goalwise.auth.security.JwtService;
import com.goalwise.auth.service.UserService;
import com.goalwise.auth.service.VonageVerifyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository users;
    private final OtpSessionRepository otps;
    private final UserService userService;
    private final VonageVerifyService vonage;
    private final JwtService jwt;

    public AuthController(UserRepository users, OtpSessionRepository otps, UserService userService,
                          VonageVerifyService vonage, JwtService jwt) {
        this.users = users;
        this.otps = otps;
        this.userService = userService;
        this.vonage = vonage;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq req) {
        if (users.existsByEmail(req.email())) {
            return ResponseEntity.status(409).body(Map.of("error","Email already exists"));
        }
        User u = userService.createUser(req.email(), req.password(), req.phone());
        // Optional: auto-start OTP for signup
        String requestId = vonage.startVerification(u.getPhone(), "SMS");
        otps.save(new OtpSession(u.getId(), requestId, "SIGNUP", "SMS"));
        return ResponseEntity.ok(new LoginResponse(requestId));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
        User u = users.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!userService.checkPassword(u, req.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        // Step-up OTP
        String requestId = vonage.startVerification(u.getPhone(), "SMS");
        otps.save(new OtpSession(u.getId(), requestId, "LOGIN", "SMS"));
        return ResponseEntity.ok(new LoginResponse(requestId));
    }

    @PostMapping("/otp/start")
    public ResponseEntity<?> startOtp(@Valid @RequestBody OtpStartReq req, Principal principal) {
        User u = null;
        if (req.email() != null) {
            u = users.findByEmail(req.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else if (principal != null) {
            u = users.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide email or be authenticated");
        }
        String requestId = vonage.startVerification(u.getPhone(), req.channel()==null ? "SMS" : req.channel());
        otps.save(new OtpSession(u.getId(), requestId, req.purpose(), req.channel()));
        return ResponseEntity.ok(new LoginResponse(requestId));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyReq req) {
        OtpSession s = otps.findByVonageRequestId(req.requestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown requestId"));
        boolean ok = vonage.checkCode(req.requestId(), req.code());
        if (!ok) {
            return ResponseEntity.status(400).body(Map.of("error","Invalid or expired code"));
        }
        s.setStatus("VERIFIED");
        otps.save(s);

        User u = users.findById(s.getUserId()).orElseThrow();
        u.setPhoneVerified(true);
        users.save(u);

        String token = jwt.issue(u.getId().toString(), u.getEmail());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
