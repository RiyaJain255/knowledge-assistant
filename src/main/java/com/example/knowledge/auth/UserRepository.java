package com.example.knowledge.auth;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** @throws org.springframework.dao.DuplicateKeyException if the email is already registered */
    public long insert(String email, String passwordHash, String displayName) {
        return jdbc.sql("""
                INSERT INTO app_user (email, password_hash, display_name)
                VALUES (:email, :hash, :name)
                RETURNING id
                """)
                .param("email", email)
                .param("hash", passwordHash)
                .param("name", displayName)
                .query(Long.class)
                .single();
    }

    public Optional<AppUser> findByEmail(String email) {
        return jdbc.sql("SELECT * FROM app_user WHERE email = :email")
                .param("email", email)
                .query(AppUser.class)
                .optional();
    }

    public Optional<AppUser> findById(long id) {
        return jdbc.sql("SELECT * FROM app_user WHERE id = :id")
                .param("id", id)
                .query(AppUser.class)
                .optional();
    }
}
