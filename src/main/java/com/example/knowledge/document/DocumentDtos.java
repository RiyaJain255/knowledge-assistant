package com.example.knowledge.document;

import java.time.OffsetDateTime;

public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record DocumentResponse(
            Long id,
            Long collectionId,
            String filename,
            String fileType,
            long sizeBytes,
            String status,
            int chunkCount,
            String errorMessage,
            OffsetDateTime createdAt) {

        public static DocumentResponse from(SourceDocument d) {
            return new DocumentResponse(d.id(), d.collectionId(), d.filename(), d.fileType(), d.sizeBytes(),
                    d.status(), d.chunkCount(), d.errorMessage(), d.createdAt());
        }
    }
}
