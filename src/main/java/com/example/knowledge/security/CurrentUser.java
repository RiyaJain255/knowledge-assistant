package com.example.knowledge.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Small helper to read the authenticated user's id from the validated JWT. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static long id(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
