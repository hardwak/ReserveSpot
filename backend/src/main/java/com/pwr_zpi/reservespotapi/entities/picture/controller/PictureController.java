package com.pwr_zpi.reservespotapi.entities.picture.controller;

import com.pwr_zpi.reservespotapi.entities.picture.dto.CreatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.PictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.UpdatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pictures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PictureController {

    private final PictureService pictureService;

    @GetMapping
    public ResponseEntity<List<PictureDto>> getAllPictures() {
        List<PictureDto> pictures = pictureService.getAllPictures();
        return ResponseEntity.ok(pictures);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PictureDto> getPictureById(@PathVariable Long id) {
        Optional<PictureDto> picture = pictureService.getPictureById(id);
        return picture.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/url/{url}")
    public ResponseEntity<List<PictureDto>> getPicturesByUrl(@PathVariable String url) {
        List<PictureDto> pictures = pictureService.getPicturesByUrl(url);
        return ResponseEntity.ok(pictures);
    }

    @GetMapping("/description/{description}")
    public ResponseEntity<List<PictureDto>> getPicturesByDescription(@PathVariable String description) {
        List<PictureDto> pictures = pictureService.getPicturesByDescription(description);
        return ResponseEntity.ok(pictures);
    }

    @PostMapping
    public ResponseEntity<PictureDto> createPicture(@Valid @RequestBody CreatePictureDto createDto) {
        PictureDto createdPicture = pictureService.createPicture(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPicture);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PictureDto> updatePicture(@PathVariable Long id, 
                                                  @Valid @RequestBody UpdatePictureDto updateDto) {
        Optional<PictureDto> updatedPicture = pictureService.updatePicture(id, updateDto);
        return updatedPicture.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePicture(@PathVariable Long id) {
        boolean deleted = pictureService.deletePicture(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPictureCount() {
        long count = pictureService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> pictureExists(@PathVariable Long id) {
        boolean exists = pictureService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
