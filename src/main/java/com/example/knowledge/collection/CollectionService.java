package com.example.knowledge.collection;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.example.knowledge.collection.CollectionDtos.CollectionResponse;
import com.example.knowledge.collection.CollectionDtos.CreateCollectionRequest;
import com.example.knowledge.common.ConflictException;
import com.example.knowledge.common.NotFoundException;
import com.example.knowledge.document.DocumentService;

@Service
public class CollectionService {

    private final CollectionRepository collections;
    private final DocumentService documents;

    public CollectionService(CollectionRepository collections, DocumentService documents) {
        this.collections = collections;
        this.documents = documents;
    }

    public CollectionResponse create(long userId, CreateCollectionRequest req) {
        String description = req.description() == null || req.description().isBlank() ? null : req.description().trim();
        long id;
        try {
            id = collections.insert(userId, req.name().trim(), description);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("You already have a collection with this name.");
        }
        return CollectionResponse.from(collections.findByIdAndUser(id, userId).orElseThrow());
    }

    public List<CollectionResponse> list(long userId) {
        return collections.findAllByUser(userId).stream().map(CollectionResponse::from).toList();
    }

    public void delete(long userId, long id) {
        collections.findByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found."));
        // Remove vectors + files first; the DB rows go with the collection (ON DELETE CASCADE).
        documents.purgeCollection(userId, id);
        collections.delete(id);
    }
}
