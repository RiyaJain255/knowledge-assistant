package com.example.knowledge.auth;

import java.time.OffsetDateTime;

public record AppUser(
        Long id,
        String email,
        String passwordHash,
        String displayName,
        OffsetDateTime createdAt) {
}
