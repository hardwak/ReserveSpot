package com.pwr_zpi.reservespotapi.entities.picture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    List<Picture> findByUrlContainingIgnoreCase(String url);
    List<Picture> findByDescriptionContainingIgnoreCase(String description);
}
