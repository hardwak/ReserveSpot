package com.pwr_zpi.reservespotapi.entities.tag.mapper;

import com.pwr_zpi.reservespotapi.entities.tag.dto.CreateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.TagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.UpdateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TagMapper {

    public TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }

        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .restaurantIds(tag.getRestaurants() != null ? 
                    tag.getRestaurants().stream().map(restaurant -> restaurant.getId()).collect(Collectors.toSet()) : null)
                .build();
    }

    public Tag toEntity(CreateTagDto dto) {
        if (dto == null) {
            return null;
        }

        return Tag.builder()
                .name(dto.getName())
                .build();
    }

    public void updateEntity(UpdateTagDto dto, Tag tag) {
        if (dto == null || tag == null) {
            return;
        }

        if (dto.getName() != null) {
            tag.setName(dto.getName());
        }
    }
}
