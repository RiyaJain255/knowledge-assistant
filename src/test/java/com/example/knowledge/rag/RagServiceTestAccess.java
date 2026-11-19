package com.example.knowledge.rag;

import java.util.Set;

/** Lives in the same package as RagService so tests can reach its package-private helpers. */
public final class RagServiceTestAccess {

    private RagServiceTestAccess() {
    }

    public static Set<Integer> cited(String answer) {
        return RagService.citedIndexes(answer);
    }
}
