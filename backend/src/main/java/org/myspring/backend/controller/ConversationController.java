package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.response.ConversationResponse;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getAllConversations(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                conversationService.getAllConversationsByUserId(principal.user().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getDetailConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                conversationService.getConversationById(id, principal.user().getId()));
    }
}
