package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.response.ConversationResponse;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.ConversationRepository;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.service.rag.TitleGeneratorService;
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
    private final TitleGeneratorService titleGeneratorService;

    public List<ConversationResponse> getAllConversationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        return conversationRepository.findAllByUser(user)
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
        conversation.setTitle(titleGeneratorService.generate(userQuestion));

        return conversationRepository.save(conversation);
    }
}
