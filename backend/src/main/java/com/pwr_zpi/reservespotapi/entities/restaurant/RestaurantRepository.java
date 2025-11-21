package com.pwr_zpi.reservespotapi.entities.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {
    List<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByCityIgnoreCase(String city);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT r FROM Restaurant r ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findTopRated(Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.tags t " +
            "WHERE t.id IN :tagIds " +
            "AND r.id NOT IN :excludedIds " +
            "ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findByTagsInAndIdNotIn(
            @Param("tagIds") Set<Long> tagIds,
            @Param("excludedIds") Set<Long> excludedIds,
            Pageable pageable
    );

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.tags t " +
            "WHERE t.id IN :tagIds " +
            "ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findByTagsIn(@Param("tagIds") Set<Long> tagIds, Pageable pageable);
}