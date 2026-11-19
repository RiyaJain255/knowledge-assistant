package com.example.knowledge.document;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private final JdbcClient jdbc;

    public DocumentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(long collectionId, long userId, String filename, String fileType,
                       long sizeBytes, String storagePath) {
        return jdbc.sql("""
                INSERT INTO source_document
                    (collection_id, user_id, filename, file_type, size_bytes, storage_path, status)
                VALUES (:cid, :uid, :fn, :ft, :size, :path, 'PROCESSING')
                RETURNING id
                """)
                .param("cid", collectionId)
                .param("uid", userId)
                .param("fn", filename)
                .param("ft", fileType)
                .param("size", sizeBytes)
                .param("path", storagePath)
                .query(Long.class)
                .single();
    }

    public Optional<SourceDocument> findById(long id) {
        return jdbc.sql("SELECT * FROM source_document WHERE id = :id")
                .param("id", id)
                .query(SourceDocument.class)
                .optional();
    }

    public Optional<SourceDocument> findByIdAndUser(long id, long userId) {
        return jdbc.sql("SELECT * FROM source_document WHERE id = :id AND user_id = :uid")
                .param("id", id)
                .param("uid", userId)
                .query(SourceDocument.class)
                .optional();
    }

    public List<SourceDocument> findByCollection(long collectionId, long userId) {
        return jdbc.sql("""
                SELECT * FROM source_document
                WHERE collection_id = :cid AND user_id = :uid
                ORDER BY created_at DESC
                """)
                .param("cid", collectionId)
                .param("uid", userId)
                .query(SourceDocument.class)
                .list();
    }

    public void updateStatus(long id, DocumentStatus status, int chunkCount, String errorMessage) {
        jdbc.sql("""
                UPDATE source_document
                SET status = :status, chunk_count = :chunks, error_message = :err, updated_at = now()
                WHERE id = :id
                """)
                .param("status", status.name())
                .param("chunks", chunkCount)
                .param("err", errorMessage, Types.VARCHAR)
                .param("id", id)
                .update();
    }

    public void delete(long id) {
        jdbc.sql("DELETE FROM source_document WHERE id = :id").param("id", id).update();
    }

    /** Called at startup: ingestion jobs cut short by a restart can never finish. */
    public int failInterruptedJobs() {
        return jdbc.sql("""
                UPDATE source_document
                SET status = 'FAILED',
                    error_message = 'Processing was interrupted by a server restart. Please upload the file again.',
                    updated_at = now()
                WHERE status = 'PROCESSING'
                """)
                .update();
    }
}
