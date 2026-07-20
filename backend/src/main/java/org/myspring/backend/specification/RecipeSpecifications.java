package org.myspring.backend.specification;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.model.Recipe;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public final class RecipeSpecifications {

    // https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html
    public static Specification<Recipe> titleContainsAllWords(String query) {
        String[] words = query.trim().split("\\s+");
        return (root, _, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            for (String word : words) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + word.toLowerCase() + "%"
                        )
                );
            }
            return predicate;
        };
    }
}
