package org.myspring.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.response.ConversationResponse;
import org.myspring.backend.enums.MessageRole;
import org.myspring.backend.model.ChatMessage;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.ConversationRepository;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.service.rag.TitleGeneratorService;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TitleGeneratorService titleGeneratorService;

    private ConversationService conversationService;

    private Conversation newConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        conversation.setTitle("Rendang Chat");
        return conversation;
    }

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(conversationRepository, userRepository, titleGeneratorService);
    }

    @Test
    void getAllConversationsByUserId_returnsConversationsWithoutMessages_whenUserExists() {
        User user = User.builder().id(1L).build();
        Conversation conversation = newConversation();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(conversationRepository.findAllByUser(user)).thenReturn(List.of(conversation));

        List<ConversationResponse> result = conversationService.getAllConversationsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.getFirst().title()).isEqualTo("Rendang Chat");
        assertThat(result.getFirst().messages()).isEmpty();
    }

    @Test
    void getAllConversationsByUserId_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> conversationService.getAllConversationsByUserId(999L));
        verify(conversationRepository, never()).findAllByUser(any());
    }

    @Test
    void getConversationById_returnsConversationWithMessages_whenFound() {
        Conversation conversation = newConversation();
        ChatMessage message = new ChatMessage(
                76L,
                "Hi buddy",
                conversation,
                MessageRole.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        conversation.setChatMessages(List.of(message));
        when(conversationRepository.findDetailByIdAndUserId(10L, 1L)).thenReturn(Optional.of(conversation));

        ConversationResponse result = conversationService.getConversationById(10L, 1L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.messages()).hasSize(1);
        assertThat(result.messages().getFirst().content()).isEqualTo("Hi buddy");
    }

    @Test
    void getConversationById_throwsNotFound_whenNotFound() {
        when(conversationRepository.findDetailByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> conversationService.getConversationById(999L, 1L));
    }

    @Test
    void getConversationIdAndByUserId_returnsConversation_whenFound() {
        User user = User.builder().id(1L).build();
        Conversation conversation = newConversation();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.getConversationIdAndByUserId(10L, 1L);

        assertThat(result).isEqualTo(conversation);
    }

    @Test
    void getConversationIdAndByUserId_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> conversationService.getConversationIdAndByUserId(10L, 999L));
        verify(conversationRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void getConversationIdAndByUserId_throwsNotFound_whenConversationDoesNotExist() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> conversationService.getConversationIdAndByUserId(999L, 1L));
    }

    @Test
    void getOrCreateConversation_returnsExistingConversation_whenConversationIdProvided() {
        User user = User.builder().id(1L).build();
        Conversation conversation = newConversation();
        ChatRequest request = new ChatRequest(10L, "What is rendang?");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.getOrCreateConversation(1L, request, "What is rendang?");

        assertThat(result).isEqualTo(conversation);
        verify(titleGeneratorService, never()).generate(any());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void getOrCreateConversation_createsNewConversation_whenConversationIdIsNull() {
        User user = User.builder().id(1L).build();
        ChatRequest request = new ChatRequest(null, "What is rendang?");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(titleGeneratorService.generate("What is rendang?")).thenReturn("Rendang Chat");
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Conversation result = conversationService.getOrCreateConversation(1L, request, "What is rendang?");

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getTitle()).isEqualTo("Rendang Chat");

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void getOrCreateConversation_throwsNotFound_whenUserDoesNotExistAndNoConversationId() {
        ChatRequest request = new ChatRequest(null, "What is rendang?");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> conversationService.getOrCreateConversation(999L, request, "What is rendang?"));
        verify(conversationRepository, never()).save(any());
    }
}
