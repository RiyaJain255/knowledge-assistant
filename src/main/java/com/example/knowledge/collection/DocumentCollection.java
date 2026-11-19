package com.example.knowledge.collection;

import java.time.OffsetDateTime;

public record DocumentCollection(
        Long id,
        Long userId,
        String name,
        String description,
        long documentCount,
        OffsetDateTime createdAt) {
}
