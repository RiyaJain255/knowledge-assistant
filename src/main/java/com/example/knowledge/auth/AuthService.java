package com.example.knowledge.auth;

import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.knowledge.auth.AuthDtos.AuthResponse;
import com.example.knowledge.auth.AuthDtos.LoginRequest;
import com.example.knowledge.auth.AuthDtos.RegisterRequest;
import com.example.knowledge.auth.AuthDtos.UserView;
import com.example.knowledge.common.AuthenticationFailedException;
import com.example.knowledge.common.ConflictException;
import com.example.knowledge.common.NotFoundException;
import com.example.knowledge.security.TokenService;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = normalize(req.email());
        long id;
        try {
            id = users.insert(email, passwordEncoder.encode(req.password()), req.displayName().trim());
        } catch (DuplicateKeyException e) {
            throw new ConflictException("An account with this email already exists.");
        }
        AppUser user = users.findById(id).orElseThrow();
        return respond(user);
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = users.findByEmail(normalize(req.email()))
                .filter(u -> passwordEncoder.matches(req.password(), u.passwordHash()))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));
        return respond(user);
    }

    public UserView me(long userId) {
        return users.findById(userId)
                .map(UserView::from)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private AuthResponse respond(AppUser user) {
        TokenService.IssuedToken token = tokens.issue(user);
        return new AuthResponse(token.value(), token.expiresAt(), UserView.from(user));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
