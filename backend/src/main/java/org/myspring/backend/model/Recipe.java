package org.myspring.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recipes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String ingredients;
    private String steps;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updateddAt;
    @ManyToMany(mappedBy = "favoriteRecipes")
    private Set<User> favoritedByUsers = new HashSet<>();
}
