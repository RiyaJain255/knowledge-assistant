package com.example.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.knowledge.rag.RagServiceTestAccess;

class RagCitationTest {

    @Test
    void findsCitationMarkers() {
        assertThat(RagServiceTestAccess.cited("Leave is 24 days [1][3]. Carry-over is 5 days [2]."))
                .containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void ignoresTextWithoutMarkers() {
        assertThat(RagServiceTestAccess.cited("No sources here, just an array a[i] and [text].")).isEmpty();
    }
}
