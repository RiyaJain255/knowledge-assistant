package com.example.knowledge.rag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.knowledge.rag.RagDtos.AskRequest;
import com.example.knowledge.rag.RagDtos.AskResponse;
import com.example.knowledge.security.CurrentUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class RagController {

    private final RagService rag;

    public RagController(RagService rag) {
        this.rag = rag;
    }

    @PostMapping("/ask")
    public AskResponse ask(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AskRequest request) {
        return rag.ask(CurrentUser.id(jwt), request);
    }
}
