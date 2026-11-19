package com.example.knowledge.collection;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CollectionDtos {

    private CollectionDtos() {
    }

    public record CreateCollectionRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description) {
    }

    public record CollectionResponse(
            Long id,
            String name,
            String description,
            long documentCount,
            OffsetDateTime createdAt) {

        public static CollectionResponse from(DocumentCollection c) {
            return new CollectionResponse(c.id(), c.name(), c.description(), c.documentCount(), c.createdAt());
        }
    }
}
