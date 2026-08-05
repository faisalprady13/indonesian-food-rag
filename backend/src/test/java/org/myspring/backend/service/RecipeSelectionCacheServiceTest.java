package org.myspring.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.ConversationContext;
import org.myspring.backend.repository.ConversationContextRepository;
import org.myspring.backend.repository.ConversationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeSelectionCacheServiceTest {

    @Mock
    private ConversationContextRepository conversationContextRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RecipeSelectionCacheService recipeSelectionCacheService;

    private Conversation stubOwnedConversation(Long conversationId, Long userId) throws UnauthorizedException {
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        when(userService.getCurrentUserId()).thenReturn(userId);
        when(conversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(conversation));
        return conversation;
    }

    @Test
    void store_createsNewContext_whenNoneExistsForConversationAndListType() throws UnauthorizedException {
        Conversation conversation = stubOwnedConversation(10L, 1L);
        when(conversationContextRepository.findByConversationIdAndListType(10L, RecipeListType.SEARCH_RESULTS))
                .thenReturn(Optional.empty());

        recipeSelectionCacheService.store("10", RecipeListType.SEARCH_RESULTS, List.of(5L, 3L, 9L));

        ArgumentCaptor<ConversationContext> captor = ArgumentCaptor.forClass(ConversationContext.class);
        verify(conversationContextRepository).save(captor.capture());

        ConversationContext saved = captor.getValue();
        assertThat(saved.getConversation()).isEqualTo(conversation);
        assertThat(saved.getListType()).isEqualTo(RecipeListType.SEARCH_RESULTS);
        assertThat(saved.getRecipeIds()).containsExactly(5L, 3L, 9L);
    }

    @Test
    void store_replacesRecipeIds_onExistingContext() throws UnauthorizedException {
        stubOwnedConversation(10L, 1L);
        ConversationContext existing = new ConversationContext();
        existing.getRecipeIds().addAll(List.of(1L, 2L));
        when(conversationContextRepository.findByConversationIdAndListType(10L, RecipeListType.SEARCH_RESULTS))
                .thenReturn(Optional.of(existing));

        recipeSelectionCacheService.store("10", RecipeListType.SEARCH_RESULTS, List.of(7L, 8L));

        ArgumentCaptor<ConversationContext> captor = ArgumentCaptor.forClass(ConversationContext.class);
        verify(conversationContextRepository).save(captor.capture());

        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(captor.getValue().getRecipeIds()).containsExactly(7L, 8L);
    }

    @Test
    void store_throwsIllegalArgumentException_whenConversationNotOwnedByCurrentUser() throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(conversationRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> recipeSelectionCacheService.store("10", RecipeListType.SEARCH_RESULTS, List.of(1L)));

        verify(conversationContextRepository, never()).save(any());
    }

    @Test
    void store_propagatesUnauthorized_whenNoAuthenticatedUser() throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenThrow(new UnauthorizedException("No authenticated user"));

        assertThrows(UnauthorizedException.class,
                () -> recipeSelectionCacheService.store("10", RecipeListType.SEARCH_RESULTS, List.of(1L)));

        verify(conversationContextRepository, never()).save(any());
    }

    @Test
    void resolvePosition_returnsRecipeIdAtGivenPosition() throws UnauthorizedException {
        stubOwnedConversation(10L, 1L);
        ConversationContext context = new ConversationContext();
        context.getRecipeIds().addAll(List.of(11L, 22L, 33L));
        when(conversationContextRepository.findByConversationIdAndListType(10L, RecipeListType.SEARCH_RESULTS))
                .thenReturn(Optional.of(context));

        Long result = recipeSelectionCacheService.resolvePosition("10", RecipeListType.SEARCH_RESULTS, 2);

        assertThat(result).isEqualTo(22L);
    }

    @Test
    void resolvePosition_throwsIllegalStateException_whenNoListHasBeenShown() throws UnauthorizedException {
        stubOwnedConversation(10L, 1L);
        when(conversationContextRepository.findByConversationIdAndListType(10L, RecipeListType.FAVORITES))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> recipeSelectionCacheService.resolvePosition("10", RecipeListType.FAVORITES, 1));
    }

    @Test
    void resolvePosition_throwsIllegalArgumentException_whenPositionTooLow() throws UnauthorizedException {
        stubOwnedConversation(10L, 1L);
        ConversationContext context = new ConversationContext();
        context.getRecipeIds().addAll(List.of(11L, 22L));
        when(conversationContextRepository.findByConversationIdAndListType(10L, RecipeListType.SEARCH_RESULTS))
                .thenReturn(Optional.of(context));

        assertThrows(IllegalArgumentException.class,
                () -> recipeSelectionCacheService.resolvePosition("10", RecipeListType.SEARCH_RESULTS, 0));
    }

    @Test
    void resolvePosition_throwsIllegalArgumentException_whenPositionTooHigh() throws UnauthorizedException {
        stubOwnedConversation(10L, 1L);
        ConversationContext context = new ConversationContext();
        context.getRecipeIds().addAll(List.of(11L, 22L));
        when(conversationContextRepository.findByConversationIdAndListType(10L, RecipeListType.SEARCH_RESULTS))
                .thenReturn(Optional.of(context));

        assertThrows(IllegalArgumentException.class,
                () -> recipeSelectionCacheService.resolvePosition("10", RecipeListType.SEARCH_RESULTS, 3));
    }

    @Test
    void resolvePosition_throwsIllegalArgumentException_whenConversationNotOwnedByCurrentUser() throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(conversationRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> recipeSelectionCacheService.resolvePosition("10", RecipeListType.SEARCH_RESULTS, 1));
    }
}
