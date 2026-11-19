package com.example.knowledge.document;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.knowledge.document.DocumentDtos.DocumentResponse;
import com.example.knowledge.security.CurrentUser;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    /** Upload returns 202: the file is accepted and indexed in the background. Poll the document's status. */
    @PostMapping(path = "/collections/{collectionId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable long collectionId,
                                                   @RequestParam("file") MultipartFile file) {
        return ResponseEntity.accepted().body(service.upload(CurrentUser.id(jwt), collectionId, file));
    }

    @GetMapping("/collections/{collectionId}/documents")
    public List<DocumentResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable long collectionId) {
        return service.list(CurrentUser.id(jwt), collectionId);
    }

    @GetMapping("/documents/{id}")
    public DocumentResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return service.get(CurrentUser.id(jwt), id);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        service.delete(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
