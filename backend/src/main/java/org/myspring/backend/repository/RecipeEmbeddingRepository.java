package org.myspring.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.myspring.backend.model.RecipeEmbedding;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//TODO: this is for learning,
// refactor with VectorStore or spring QuestionAnswerAdvisor later
public interface RecipeEmbeddingRepository
        extends JpaRepository<RecipeEmbedding, Long> {

    // <-> order by distance
    @Query(value = """
            SELECT recipe_id
            FROM recipe_embedding
            ORDER BY embedding <-> CAST(:embedding AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Long> findSimilarRecipeIds(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

}