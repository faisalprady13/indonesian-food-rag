package org.myspring.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RecipeServiceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private List<Recipe> savedRecipes;
    private RequestPostProcessor asUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();
        recipeRepository.deleteAll();

        User testUser = userRepository.save(newUser());
        asUser = user(new UserPrincipal(testUser));

        savedRecipes = recipeRepository.saveAll(List.of(
                newRecipe("Rendang", "Simmer beef in coconut milk and spices.", "beef", "coconut milk", "chili"),
                newRecipe("Soto Ayam", "Simmer chicken in turmeric broth.", "chicken", "turmeric", "lemongrass")
        ));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        recipeRepository.deleteAll();
    }

    private User newUser() {
        User user = new User();
        user.setUsername("chef");
        user.setFullname("Test Chef");
        user.setEmail("chef" + "@example.com");
        user.setPassword("password");
        return user;
    }

    private Recipe newRecipe(String title, String steps, String... ingredientNames) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setSteps(steps);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        Set<Ingredient> ingredients = Arrays.stream(ingredientNames)
                .map(name -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(name);
                    ingredient.setRecipe(recipe);
                    return ingredient;
                })
                .collect(Collectors.toSet());
        recipe.setIngredients(ingredients);
        return recipe;
    }

    private Recipe recipeByTitle(String title) {
        return savedRecipes.stream()
                .filter(recipe -> recipe.getTitle().equals(title))
                .findFirst()
                .orElseThrow();
    }

    private void markAsFavorite(Recipe recipe) throws Exception {
        mockMvc.perform(post("/api/recipe/" + recipe.getId() + "/favorite").with(asUser))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRecipes_returnsAllRecipesPaginated() throws Exception {
        mockMvc.perform(get("/api/recipe").with(asUser).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getRecipes_filtersBySearchTerm() throws Exception {
        mockMvc.perform(get("/api/recipe").with(asUser).param("search", "rendang"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Rendang"));
    }

    @Test
    void getRecipes_returnsEmptyPage_whenSearchMatchesNothing() throws Exception {
        mockMvc.perform(get("/api/recipe").with(asUser).param("search", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getRecipes_marksFavoritedRecipe() throws Exception {
        markAsFavorite(recipeByTitle("Rendang"));

        mockMvc.perform(get("/api/recipe").with(asUser).param("sortBy", "id").param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].favorited").value(true))
                .andExpect(jsonPath("$.content[1].favorited").value(false));
    }

    @Test
    void getRecipe_returnsRecipeWithIngredients() throws Exception {
        Recipe rendang = recipeByTitle("Rendang");

        mockMvc.perform(get("/api/recipe/" + rendang.getId()).with(asUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Rendang"))
                .andExpect(jsonPath("$.ingredients", hasSize(3)))
                .andExpect(jsonPath("$.favorited").value(false));
    }

    @Test
    void getRecipe_returnsNotFound_whenRecipeDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/recipe/999999").with(asUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFavorite_marksRecipeAsFavorited() throws Exception {
        Recipe rendang = recipeByTitle("Rendang");

        mockMvc.perform(post("/api/recipe/" + rendang.getId() + "/favorite").with(asUser))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/recipe/" + rendang.getId()).with(asUser))
                .andExpect(jsonPath("$.favorited").value(true));
    }

    @Test
    void addFavorite_returnsNotFound_whenRecipeDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/recipe/999999/favorite").with(asUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavorite_unmarksRecipeAsFavorited() throws Exception {
        Recipe rendang = recipeByTitle("Rendang");
        markAsFavorite(rendang);

        mockMvc.perform(delete("/api/recipe/" + rendang.getId() + "/favorite").with(asUser))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/recipe/" + rendang.getId()).with(asUser))
                .andExpect(jsonPath("$.favorited").value(false));
    }

    @Test
    void getFavoriteRecipes_returnsOnlyFavoritedRecipes() throws Exception {
        markAsFavorite(recipeByTitle("Rendang"));

        mockMvc.perform(get("/api/recipe/favorites").with(asUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Rendang"))
                .andExpect(jsonPath("$.content[0].favorited").value(true));
    }

    @Test
    void getFavoriteRecipes_filtersBySearchTerm() throws Exception {
        markAsFavorite(recipeByTitle("Rendang"));
        markAsFavorite(recipeByTitle("Soto Ayam"));

        mockMvc.perform(get("/api/recipe/favorites").with(asUser).param("search", "soto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Soto Ayam"));
    }

    @Test
    void getFavoriteRecipes_returnsEmptyPage_whenNothingFavorited() throws Exception {
        mockMvc.perform(get("/api/recipe/favorites").with(asUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void autocomplete_returnsMatchingTitlesOrderedByTitle() throws Exception {
        mockMvc.perform(get("/api/recipe/autocomplete").with(asUser).param("query", "s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Soto Ayam"));
    }

    @Test
    void autocomplete_returnsEmptyList_whenQueryIsBlank() throws Exception {
        mockMvc.perform(get("/api/recipe/autocomplete").with(asUser).param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void autocomplete_matchesTitle_whenQueryWordsAreOutOfOrderAndPartial() throws Exception {
        recipeRepository.save(newRecipe("Apples and Orange", "Peel and slice.", "apple", "orange"));

        mockMvc.perform(get("/api/recipe/autocomplete").with(asUser).param("query", "Orange Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Apples and Orange"));
    }

    @Test
    void autocomplete_isCaseInsensitiveAcrossMultipleWords() throws Exception {
        recipeRepository.save(newRecipe("Apples and Orange", "Peel and slice.", "apple", "orange"));

        mockMvc.perform(get("/api/recipe/autocomplete").with(asUser).param("query", "APPLE oraNGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Apples and Orange"));
    }

    @Test
    void autocomplete_returnsNoMatches_whenOneWordIsMissingFromTitle() throws Exception {
        recipeRepository.save(newRecipe("Apples and Orange", "Peel and slice.", "apple", "orange"));

        mockMvc.perform(get("/api/recipe/autocomplete").with(asUser).param("query", "Apple Banana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void autocomplete_respectsLimitParameter() throws Exception {
        recipeRepository.save(newRecipe("Soto Betawi", "Simmer beef in spiced broth.", "beef", "broth"));

        mockMvc.perform(get("/api/recipe/autocomplete").with(asUser).param("query", "soto").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Soto Ayam"));
    }
}
