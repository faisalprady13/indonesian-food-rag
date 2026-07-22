package org.myspring.backend.repository;

import org.myspring.backend.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Recipe> findByFavoritedByUsers_Id(Long userId, Pageable pageable);

    Page<Recipe> findByFavoritedByUsers_IdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);
}
