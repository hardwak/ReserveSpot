package com.pwr_zpi.reservespotapi.entities.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {
    List<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByCityIgnoreCase(String city);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT DISTINCT r.city FROM Restaurant r WHERE r.city IS NOT NULL ORDER BY r.city")
    List<String> findAllCities();

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
            "AND r.id NOT IN :excludedIds " +
            "AND LOWER(r.city) = LOWER(:city) " +
            "ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findByTagsInAndIdNotInAndCity(@Param("tagIds") Set<Long> tagIds, @Param("excludedIds") Set<Long> excludedIds, @Param("city") String city, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.tags t " +
            "WHERE t.id IN :tagIds " +
            "AND LOWER(r.city) = LOWER(:city) " +
            "ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findByTagsInAndCity(@Param("tagIds") Set<Long> tagIds, @Param("city") String city, Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.city) = LOWER(:city) ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findTopRatedByCity(@Param("city") String city, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.tags t " +
            "WHERE t.id IN :tagIds " +
            "ORDER BY r.averageRating DESC NULLS LAST")
    List<Restaurant> findByTagsIn(@Param("tagIds") Set<Long> tagIds, Pageable pageable);

    @Query(value = "SELECT * FROM restaurants WHERE id NOT IN :excludedIds ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Restaurant> findRandomNotIn(@Param("excludedIds") Collection<Long> excludedIds, @Param("limit") int limit);

    @Query(value = "SELECT * FROM restaurants WHERE id NOT IN :excludedIds AND LOWER(city) = LOWER(:city) ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Restaurant> findRandomNotInAndCity(@Param("excludedIds") Collection<Long> excludedIds, @Param("city") String city, @Param("limit") int limit);

    @Query(value = "SELECT * FROM restaurants WHERE LOWER(city) = LOWER(:city) ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Restaurant> findRandomByCity(@Param("city") String city, @Param("limit") int limit);

    @Query(value = "SELECT * FROM restaurants ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Restaurant> findRandom(@Param("limit") int limit);
}