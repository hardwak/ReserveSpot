package com.pwr_zpi.reservespotapi.entities.tag.service;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.tag.dto.CreateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.TagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.UpdateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.tag.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final RestaurantRepository restaurantRepository;

    public List<TagDto> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toDto)
                .toList();
    }

    public Optional<TagDto> getTagById(Long id) {
        return tagRepository.findById(id)
                .map(tagMapper::toDto);
    }

    public Optional<TagDto> getTagByName(String name) {
        return tagRepository.findByNameIgnoreCase(name)
                .map(tagMapper::toDto);
    }

    public TagDto createTag(CreateTagDto createDto) {
        Tag tag = tagMapper.toEntity(createDto);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }

    public Optional<TagDto> updateTag(Long id, UpdateTagDto updateDto) {
        return tagRepository.findById(id)
                .map(tag -> {
                    tagMapper.updateEntity(updateDto, tag);
                    Tag savedTag = tagRepository.save(tag);
                    return tagMapper.toDto(savedTag);
                });
    }

    public boolean deleteTag(Long id) {
        return tagRepository.findById(id)
            .map(tag -> {
                // Remove this tag from all restaurants that have it
                if (tag.getRestaurants() != null && !tag.getRestaurants().isEmpty()) {
                    // Create a copy of the set to avoid ConcurrentModificationException
                    List<Restaurant> restaurantsWithTag = tag.getRestaurants().stream().toList();
                    for (Restaurant restaurant : restaurantsWithTag) {
                        if (restaurant.getTags() != null) {
                            restaurant.getTags().remove(tag);
                            restaurantRepository.save(restaurant);
                        }
                    }
                }
                // Now delete the tag
                tagRepository.delete(tag);
                return true;
            })
            .orElse(false);
    }

    public boolean existsById(Long id) {
        return tagRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return tagRepository.existsByNameIgnoreCase(name);
    }

    public long count() {
        return tagRepository.count();
    }
}
