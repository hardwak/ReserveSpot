package com.pwr_zpi.reservespotapi.entities.tag.service;

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
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return true;
        }
        return false;
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
