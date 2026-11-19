package com.example.knowledge.rag;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class RagDtos {

    private RagDtos() {
    }

    /**
     * @param collectionId limit the search to one collection; null searches all of the user's collections
     * @param topK         optional override for how many excerpts are retrieved (1-20)
     */
    public record AskRequest(
            @NotBlank @Size(max = 2000) String question,
            Long collectionId,
            @Min(1) @Max(20) Integer topK) {
    }

    /** One retrieved excerpt. {@code index} is the [n] marker used in the answer; {@code cited} = the answer used it. */
    public record SourceCitation(
            int index,
            Long documentId,
            String filename,
            Integer page,
            Integer chunkIndex,
            double score,
            boolean cited,
            String snippet) {
    }

    public record AskResponse(String answer, List<SourceCitation> sources, long latencyMs) {
    }
}
