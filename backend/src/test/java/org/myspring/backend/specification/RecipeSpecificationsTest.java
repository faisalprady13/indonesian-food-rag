package org.myspring.backend.specification;

import org.junit.jupiter.api.Test;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RecipeSpecificationsTest {

    @Autowired
    private RecipeRepository recipeRepository;

    private void saveRecipe(String title) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipeRepository.save(recipe);
    }

    private Page<Recipe> search(String query) {
        Specification<Recipe> spec = RecipeSpecifications.titleContainsAllWords(query);
        return recipeRepository.findAll(spec, PageRequest.of(0, 10));
    }

    @Test
    void matchesTitle_regardlessOfWordOrder() {
        saveRecipe("Apples and Orange");
        saveRecipe("Rendang");

        assertThat(search("Orange Apple").getContent())
                .extracting(Recipe::getTitle)
                .containsExactly("Apples and Orange");
    }

    @Test
    void matchesTitle_withPartialWordsCaseInsensitively() {
        saveRecipe("Apples and Orange");

        assertThat(search("APPLE oraNGE").getContent())
                .extracting(Recipe::getTitle)
                .containsExactly("Apples and Orange");
    }

    @Test
    void requiresEveryWordToBePresentInTitle() {
        saveRecipe("Apples and Orange");

        assertThat(search("Apple Banana").getContent()).isEmpty();
    }

    @Test
    void matchesSingleWordAsSubstringAnywhereInTitle() {
        saveRecipe("Rendang");
        saveRecipe("Soto Ayam");

        assertThat(search("end").getContent())
                .extracting(Recipe::getTitle)
                .containsExactly("Rendang");
    }
}
