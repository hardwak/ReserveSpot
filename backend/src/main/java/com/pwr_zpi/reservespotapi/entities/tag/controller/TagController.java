package com.pwr_zpi.reservespotapi.entities.tag;

import com.pwr_zpi.reservespotapi.entities.tag.dto.CreateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.TagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.UpdateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(@PathVariable Long id) {
        Optional<TagDto> tag = tagService.getTagById(id);
        return tag.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagDto> getTagByName(@PathVariable String name) {
        Optional<TagDto> tag = tagService.getTagByName(name);
        return tag.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody CreateTagDto createDto) {
        TagDto createdTag = tagService.createTag(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> updateTag(@PathVariable Long id, 
                                           @Valid @RequestBody UpdateTagDto updateDto) {
        Optional<TagDto> updatedTag = tagService.updateTag(id, updateDto);
        return updatedTag.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        boolean deleted = tagService.deleteTag(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTagCount() {
        long count = tagService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> tagExists(@PathVariable Long id) {
        boolean exists = tagService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> tagExistsByName(@PathVariable String name) {
        boolean exists = tagService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}
