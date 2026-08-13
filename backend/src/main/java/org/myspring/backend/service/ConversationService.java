package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.request.UpdatePinnedConversationRequest;
import org.myspring.backend.dto.request.UpdateTitleConversationRequest;
import org.myspring.backend.dto.response.ConversationResponse;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.ConversationRepository;
import org.myspring.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public List<ConversationResponse> getAllConversationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        return conversationRepository.findAllByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(ConversationResponse::fromConversationWithoutMessages)
                .toList();
    }

    @Transactional
    public ConversationResponse getConversationById(Long id, Long userId) {
        Conversation conversation = conversationRepository.findDetailByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));

        return ConversationResponse.fromConversation(conversation);
    }

    public Conversation getConversationIdAndByUserId(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation not found"
                        )
                );
    }

    @Transactional
    public Conversation getOrCreateConversation(Long userId, ChatRequest request, String userQuestion) {
        if (request.conversationId() != null) {
            return getConversationIdAndByUserId(
                    request.conversationId(),
                    userId
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(placeholderTitle(userQuestion));

        return conversationRepository.save(conversation);
    }

    @Transactional
    public void updateGeneratedTitle(Long conversationId, String title) {
        conversationRepository.findById(conversationId)
                .ifPresent(conversation -> conversation.setTitle(title));
    }

    private static final int PLACEHOLDER_TITLE_MAX_LENGTH = 50;

    private String placeholderTitle(String userQuestion) {
        String trimmed = userQuestion.strip();

        return trimmed.length() > PLACEHOLDER_TITLE_MAX_LENGTH
                ? trimmed.substring(0, PLACEHOLDER_TITLE_MAX_LENGTH) + "…"
                : trimmed;
    }

    @Transactional
    public ConversationResponse updateConversationTitle(Long id, UpdateTitleConversationRequest updateTitleConversationRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        Conversation conversation = conversationRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation not found"
                        )
                );

        conversation.setTitle(updateTitleConversationRequest.title());

        return ConversationResponse.fromConversationWithoutMessages(conversation);
    }

    @Transactional
    public ConversationResponse updateConversationPinned(Long id, UpdatePinnedConversationRequest updatePinnedConversationRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        Conversation conversation = conversationRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation not found"
                        )
                );

        conversation.setPinned(updatePinnedConversationRequest.pinned());

        return ConversationResponse.fromConversationWithoutMessages(conversation);
    }

    @Transactional
    public void deleteConversationById(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));

        conversationRepository.deleteByIdAndUserId(id, user.getId());
    }
}
