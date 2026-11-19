package com.example.knowledge.collection;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionRepository {

    private static final String SELECT = """
            SELECT c.id, c.user_id, c.name, c.description, c.created_at,
                   (SELECT count(*) FROM source_document d WHERE d.collection_id = c.id) AS document_count
            FROM collection c
            """;

    private final JdbcClient jdbc;

    public CollectionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** @throws org.springframework.dao.DuplicateKeyException if the user already has a collection with this name */
    public long insert(long userId, String name, String description) {
        return jdbc.sql("""
                INSERT INTO collection (user_id, name, description)
                VALUES (:uid, :name, :desc)
                RETURNING id
                """)
                .param("uid", userId)
                .param("name", name)
                .param("desc", description)
                .query(Long.class)
                .single();
    }

    public List<DocumentCollection> findAllByUser(long userId) {
        return jdbc.sql(SELECT + " WHERE c.user_id = :uid ORDER BY c.created_at DESC")
                .param("uid", userId)
                .query(DocumentCollection.class)
                .list();
    }

    /** Ownership-checked lookup: returns empty when the collection exists but belongs to someone else. */
    public Optional<DocumentCollection> findByIdAndUser(long id, long userId) {
        return jdbc.sql(SELECT + " WHERE c.id = :id AND c.user_id = :uid")
                .param("id", id)
                .param("uid", userId)
                .query(DocumentCollection.class)
                .optional();
    }

    public void delete(long id) {
        jdbc.sql("DELETE FROM collection WHERE id = :id").param("id", id).update();
    }
}
