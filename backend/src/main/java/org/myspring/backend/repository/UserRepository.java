package org.myspring.backend.repository;

import org.myspring.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("select r.id from User u join u.favoriteRecipes r where u.id = :userId")
    Set<Long> findFavoriteRecipeIds(@Param("userId") Long userId);
}
