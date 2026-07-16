package org.myspring.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WithMockUser
class RecipeServiceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeRepository recipeRepository;

    private MockMvc mockMvc;

    private List<Recipe> savedRecipes;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        recipeRepository.deleteAll();
        savedRecipes = recipeRepository.saveAll(List.of(
                newRecipe("Rendang", "Simmer beef in coconut milk and spices.", "beef", "coconut milk", "chili"),
                newRecipe("Soto Ayam", "Simmer chicken in turmeric broth.", "chicken", "turmeric", "lemongrass")
        ));
    }

    @AfterEach
    void tearDown() {
        recipeRepository.deleteAll();
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

    @Test
    void getRecipes_returnsAllRecipesPaginated() throws Exception {
        mockMvc.perform(get("/api/recipe").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getRecipes_filtersBySearchTerm() throws Exception {
        mockMvc.perform(get("/api/recipe").param("search", "rendang"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Rendang"));
    }

    @Test
    void getRecipes_returnsEmptyPage_whenSearchMatchesNothing() throws Exception {
        mockMvc.perform(get("/api/recipe").param("search", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getRecipe_returnsRecipeWithIngredients() throws Exception {
        Recipe rendang = savedRecipes.stream()
                .filter(recipe -> recipe.getTitle().equals("Rendang"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/recipe/" + rendang.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Rendang"))
                .andExpect(jsonPath("$.ingredients", hasSize(3)));
    }

    @Test
    void getRecipe_returnsNotFound_whenRecipeDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/recipe/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void autocomplete_returnsMatchingTitlesOrderedByTitle() throws Exception {
        mockMvc.perform(get("/api/recipe/autocomplete").param("query", "s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Soto Ayam"));
    }

    @Test
    void autocomplete_returnsEmptyList_whenQueryIsBlank() throws Exception {
        mockMvc.perform(get("/api/recipe/autocomplete").param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
