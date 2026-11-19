package com.example.knowledge.auth;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72, message = "must be between 8 and 72 characters") String password,
            @NotBlank @Size(max = 100) String displayName) {
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password) {
    }

    public record UserView(Long id, String email, String displayName) {
        public static UserView from(AppUser u) {
            return new UserView(u.id(), u.email(), u.displayName());
        }
    }

    public record AuthResponse(String token, Instant expiresAt, UserView user) {
    }
}
