package com.example.knowledge.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue Storage storage,
        @DefaultValue Rag rag) {

    public record Jwt(
            @DefaultValue("") String secret,
            @DefaultValue("PT12H") Duration ttl) {
    }

    public record Storage(
            @DefaultValue("./data/uploads") String dir) {
    }

    public record Rag(
            @DefaultValue("800") int chunkSize,
            @DefaultValue("350") int minChunkSizeChars,
            @DefaultValue("5000") int maxChunksPerDocument,
            @DefaultValue("5") int topK,
            @DefaultValue("0.25") double similarityThreshold) {
    }
}
