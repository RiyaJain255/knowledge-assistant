package com.example.knowledge.common;

/** The embedding / LLM provider (or vector search) failed. Mapped to HTTP 502. */
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
