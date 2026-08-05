package org.myspring.backend.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.RecipeDto;
import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.factory.EmbeddingModelFactory;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.RecipeSelectionCacheService;
import org.myspring.backend.service.UserService;
import org.myspring.backend.service.UserSettingService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeToolsTest {

    private static final String CONVERSATION_ID = "10";
    private static final ToolContext TOOL_CONTEXT = new ToolContext(Map.of("conversationId", CONVERSATION_ID));
    private static final String EMBEDDING_MODEL = "text-embedding-3-small";

    @Mock
    private RecipeEmbeddingRepository embeddingRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private EmbeddingModelFactory embeddingModelFactory;

    @Mock
    private UserService userService;

    @Mock
    private UserSettingService userSettingService;

    @Mock
    private ApiKeyEncryptionService apiKeyEncryptionService;

    @Mock
    private RecipeSelectionCacheService recipeSelectionCacheService;

    @Mock
    private OpenAiEmbeddingModel embeddingModel;

    private RecipeTools recipeTools;

    @BeforeEach
    void setUp() {
        recipeTools = new RecipeTools(
                embeddingRepository,
                recipeRepository,
                embeddingModelFactory,
                userService,
                userSettingService,
                apiKeyEncryptionService,
                recipeSelectionCacheService
        );
        ReflectionTestUtils.setField(recipeTools, "embeddingModelName", EMBEDDING_MODEL);
    }

    private Recipe recipe(Long id, String title) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setTitle(title);
        recipe.setSteps("Step 1. Mix. Step 2. Bake.");
        return recipe;
    }

    private void stubEmbeddingSearch(String query, List<Long> rankedRecipeIds) throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userSettingService.findByUserId(1L)).thenReturn(UserSetting.builder().apiKey("encrypted-key").build());
        when(apiKeyEncryptionService.decrypt("encrypted-key")).thenReturn("sk-test-key");
        when(embeddingModelFactory.create("sk-test-key", EMBEDDING_MODEL)).thenReturn(embeddingModel);
        when(embeddingModel.embed(query)).thenReturn(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingRepository.findSimilarRecipeIds(anyString(), eq(5))).thenReturn(rankedRecipeIds);
    }

    @Test
    void searchRecipes_preservesSimilarityRankedOrder_evenWhenRepositoryReturnsRecipesInADifferentOrder()
            throws UnauthorizedException {
        stubEmbeddingSearch("banana bread", List.of(3L, 1L, 2L));

        Recipe recipeOne = recipe(1L, "Recipe One");
        Recipe recipeTwo = recipe(2L, "Recipe Two");
        Recipe recipeThree = recipe(3L, "Recipe Three");
        // findAllById does not preserve the requested ID order - simulate that here.
        when(recipeRepository.findAllById(List.of(3L, 1L, 2L)))
                .thenReturn(List.of(recipeOne, recipeTwo, recipeThree));

        List<RecipeDto> result = recipeTools.searchRecipes("banana bread", 5, TOOL_CONTEXT);

        assertThat(result).extracting(RecipeDto::id).containsExactly(3L, 1L, 2L);
        verify(recipeSelectionCacheService)
                .store(CONVERSATION_ID, RecipeListType.SEARCH_RESULTS, List.of(3L, 1L, 2L));
    }

    @Test
    void searchRecipes_returnsEmptyList_andSkipsCache_whenNoSimilarRecipesFound() throws UnauthorizedException {
        stubEmbeddingSearch("nonexistent dish", List.of());

        List<RecipeDto> result = recipeTools.searchRecipes("nonexistent dish", 5, TOOL_CONTEXT);

        assertThat(result).isEmpty();
        verify(recipeRepository, never()).findAllById(any());
        verify(recipeSelectionCacheService, never()).store(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecipesByTitle_returnsMatches_andStoresIntoCache() throws UnauthorizedException {
        Recipe recipe = recipe(5L, "Rendang");
        when(recipeRepository.findAll(any(Specification.class))).thenReturn(List.of(recipe));

        List<RecipeDto> result = recipeTools.getRecipesByTitle("Rendang", TOOL_CONTEXT);

        assertThat(result).extracting(RecipeDto::id).containsExactly(5L);
        verify(recipeSelectionCacheService)
                .store(CONVERSATION_ID, RecipeListType.SEARCH_RESULTS, List.of(5L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecipesByTitle_throwsIllegalArgumentException_whenNoneFound() throws UnauthorizedException {
        when(recipeRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> recipeTools.getRecipesByTitle("Ghost Recipe", TOOL_CONTEXT));

        verify(recipeSelectionCacheService, never()).store(any(), any(), any());
    }

    @Test
    void getRecipeByListPosition_resolvesPositionFromCache_thenReturnsFullRecipe() throws UnauthorizedException {
        Recipe recipe = recipe(42L, "Soto Ayam");
        when(recipeSelectionCacheService.resolvePosition(CONVERSATION_ID, RecipeListType.SEARCH_RESULTS, 2))
                .thenReturn(42L);
        when(recipeRepository.findById(42L)).thenReturn(Optional.of(recipe));

        RecipeDto result = recipeTools.getRecipeByListPosition(2, TOOL_CONTEXT);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.title()).isEqualTo("Soto Ayam");
    }

    @Test
    void getRecipeByListPosition_throwsIllegalArgumentException_whenRecipeNoLongerExists()
            throws UnauthorizedException {
        when(recipeSelectionCacheService.resolvePosition(CONVERSATION_ID, RecipeListType.SEARCH_RESULTS, 2))
                .thenReturn(42L);
        when(recipeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> recipeTools.getRecipeByListPosition(2, TOOL_CONTEXT));
    }
}
