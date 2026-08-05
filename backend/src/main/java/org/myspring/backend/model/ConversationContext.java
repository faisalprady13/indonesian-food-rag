package org.myspring.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.myspring.backend.enums.RecipeListType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "conversation_contexts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "list_type"})
)
@Getter
@Setter
@NoArgsConstructor
public class ConversationContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", nullable = false)
    private RecipeListType listType;

    @ElementCollection
    @CollectionTable(
            name = "conversation_context_recipe_ids",
            joinColumns = @JoinColumn(name = "conversation_context_id", nullable = false)
    )
    @OrderColumn(name = "list_position", nullable = false)
    @Column(name = "recipe_id", nullable = false)
    private List<Long> recipeIds = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
