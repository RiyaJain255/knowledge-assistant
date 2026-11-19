package com.example.knowledge.document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.knowledge.collection.CollectionRepository;
import com.example.knowledge.common.BadRequestException;
import com.example.knowledge.common.NotFoundException;
import com.example.knowledge.document.DocumentDtos.DocumentResponse;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "docx", "txt");

    private final DocumentRepository documents;
    private final CollectionRepository collections;
    private final FileStorageService storage;
    private final IngestionService ingestion;
    private final VectorStore vectorStore;

    public DocumentService(DocumentRepository documents,
                           CollectionRepository collections,
                           FileStorageService storage,
                           IngestionService ingestion,
                           VectorStore vectorStore) {
        this.documents = documents;
        this.collections = collections;
        this.storage = storage;
        this.ingestion = ingestion;
        this.vectorStore = vectorStore;
    }

    /** Validates and stores the file, then kicks off background indexing. Returns immediately (status PROCESSING). */
    public DocumentResponse upload(long userId, long collectionId, MultipartFile file) {
        collections.findByIdAndUser(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found."));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("The uploaded file is empty.");
        }
        String filename = cleanFilename(file.getOriginalFilename());
        String extension = extensionOf(filename);
        if (!ALLOWED_TYPES.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Please upload a PDF, DOCX or TXT file.");
        }
        verifySignature(file, extension);

        Path stored = storage.store(file, userId, extension);
        long id = documents.insert(collectionId, userId, filename, extension, file.getSize(), stored.toString());
        ingestion.ingest(id); // @Async: returns right away

        return DocumentResponse.from(documents.findById(id).orElseThrow());
    }

    public List<DocumentResponse> list(long userId, long collectionId) {
        collections.findByIdAndUser(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found."));
        return documents.findByCollection(collectionId, userId).stream().map(DocumentResponse::from).toList();
    }

    public DocumentResponse get(long userId, long id) {
        return documents.findByIdAndUser(id, userId)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new NotFoundException("Document not found."));
    }

    public void delete(long userId, long id) {
        SourceDocument doc = documents.findByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Document not found."));
        ingestion.deleteVectors(doc.id());
        storage.deleteQuietly(doc.storagePath());
        documents.delete(doc.id());
    }

    /** Removes every vector and file belonging to a collection (called before the collection row is deleted). */
    public void purgeCollection(long userId, long collectionId) {
        List<SourceDocument> docs = documents.findByCollection(collectionId, userId);
        Filter.Expression byCollection = new FilterExpressionBuilder().eq("collectionId", collectionId).build();
        vectorStore.delete(byCollection);
        docs.forEach(d -> storage.deleteQuietly(d.storagePath()));
    }

    // --- helpers -----------------------------------------------------------------------------

    private static String cleanFilename(String original) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(original == null ? "" : original));
        if (name == null || name.isBlank()) {
            return "upload";
        }
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Cheap sanity check on the first bytes so a renamed file doesn't reach the parsers. */
    private static void verifySignature(MultipartFile file, String extension) {
        try {
            byte[] head = file.getInputStream().readNBytes(4);
            boolean ok = switch (extension) {
                case "pdf" -> head.length >= 4 && head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F';
                case "docx" -> head.length >= 2 && head[0] == 'P' && head[1] == 'K'; // .docx is a ZIP container
                default -> true;
            };
            if (!ok) {
                throw new BadRequestException("The file content does not match its ." + extension + " extension.");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
