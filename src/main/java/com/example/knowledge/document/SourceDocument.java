package com.example.knowledge.document;

import java.time.OffsetDateTime;

/** A file a user uploaded. Its text chunks live in the vector_store table (metadata.documentId = id). */
public record SourceDocument(
        Long id,
        Long collectionId,
        Long userId,
        String filename,
        String fileType,
        long sizeBytes,
        String storagePath,
        String status,
        int chunkCount,
        String errorMessage,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
