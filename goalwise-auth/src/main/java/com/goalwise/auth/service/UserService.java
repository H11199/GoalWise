package com.goalwise.auth.service;

import com.goalwise.auth.entity.User;
import com.goalwise.auth.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User createUser(String email, String rawPassword, String phone) {
        User u = new User(email, encoder.encode(rawPassword), phone);
        return users.save(u);
    }

    public Optional<User> findByEmail(String email) { return users.findByEmail(email); }

    public boolean checkPassword(User u, String raw) { return encoder.matches(raw, u.getPasswordHash()); }
}
