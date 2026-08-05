package org.myspring.backend.model;


import com.pgvector.PGvector;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_embedding")
public class RecipeEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(columnDefinition = "vector(1536)")
    private PGvector embedding;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}