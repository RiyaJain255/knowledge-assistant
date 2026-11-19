package com.example.knowledge.document;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.knowledge.config.AppProperties;
import com.example.knowledge.document.DocumentTextExtractor.Section;

/**
 * The ingestion pipeline, run in the background after an upload:
 * extract text -> split into chunks -> embed + store in pgvector.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final DocumentRepository documents;
    private final DocumentTextExtractor extractor;
    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;
    private final int maxChunks;

    public IngestionService(DocumentRepository documents,
                            DocumentTextExtractor extractor,
                            VectorStore vectorStore,
                            AppProperties props) {
        this.documents = documents;
        this.extractor = extractor;
        this.vectorStore = vectorStore;
        this.maxChunks = props.rag().maxChunksPerDocument();
        // (chunkSize tokens, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator)
        // maxNumChunks is set very high on purpose: the splitter would silently drop the rest of a long
        // document, so the limit is enforced explicitly below with a clear error instead.
       this.splitter = new TokenTextSplitter(
        props.rag().chunkSize(),
        props.rag().minChunkSizeChars(),
        5,
        100_000,
        true,
        List.of('\n', ' ', '.', ',', ';', ':', '!', '?')
);
    }

    @Async("ingestionExecutor")
    public void ingest(long documentId) {
        SourceDocument doc = documents.findById(documentId).orElse(null);
        if (doc == null) {
            return;
        }
        long started = System.currentTimeMillis();
        try {
            List<Section> sections = extractor.extract(Path.of(doc.storagePath()), doc.fileType());
            if (sections.isEmpty()) {
                throw new IllegalStateException(
                    "No extractable text found. Scanned/image-only documents need OCR, which isn't supported.");
            }

            List<Document> pages = sections.stream().map(s -> toDocument(doc, s)).toList();
            List<Document> chunks = splitter.apply(pages);
            if (chunks.isEmpty()) {
                throw new IllegalStateException("The document contains too little text to index.");
            }
            if (chunks.size() > maxChunks) {
                throw new IllegalStateException("Document is too large to index (" + chunks.size()
                        + " chunks, limit " + maxChunks + "). Split it into smaller files.");
            }

            List<Document> indexed = IntStream.range(0, chunks.size())
                    .mapToObj(i -> chunks.get(i).mutate().metadata("chunkIndex", i).build())
                    .toList();

            vectorStore.add(indexed); // calls the embedding model, then inserts into vector_store

            // If the user deleted the document while we were embedding, don't leave orphan vectors behind.
            if (documents.findById(documentId).isEmpty()) {
                deleteVectors(documentId);
                return;
            }
            documents.updateStatus(documentId, DocumentStatus.READY, indexed.size(), null);
            log.info("Indexed document {} ({}) into {} chunks in {} ms",
                    documentId, doc.filename(), indexed.size(), System.currentTimeMillis() - started);
        } catch (Exception e) {
            log.warn("Ingestion failed for document {} ({}): {}", documentId, doc.filename(), e.toString());
            deleteVectorsQuietly(documentId);
            documents.updateStatus(documentId, DocumentStatus.FAILED, 0, friendlyMessage(e));
        }
    }

    private Document toDocument(SourceDocument doc, Section section) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", doc.userId());
        metadata.put("collectionId", doc.collectionId());
        metadata.put("documentId", doc.id());
        metadata.put("filename", doc.filename());
        metadata.put("fileType", doc.fileType());
        if (section.page() != null) {
            metadata.put("page", section.page());
        }
        return Document.builder().text(section.text()).metadata(metadata).build();
    }

    /** Removes all vector chunks that belong to one document. */
    public void deleteVectors(long documentId) {
        Filter.Expression byDocument = new FilterExpressionBuilder().eq("documentId", documentId).build();
        vectorStore.delete(byDocument);
    }

    private void deleteVectorsQuietly(long documentId) {
        try {
            deleteVectors(documentId);
        } catch (Exception ex) {
            log.warn("Could not clean up vectors for document {}: {}", documentId, ex.getMessage());
        }
    }

    private static String friendlyMessage(Exception e) {
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (message.length() > 900) {
            message = message.substring(0, 900) + "...";
        }
        return message;
    }
}
