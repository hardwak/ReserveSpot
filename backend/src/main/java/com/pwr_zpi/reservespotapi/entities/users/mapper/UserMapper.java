package com.pwr_zpi.reservespotapi.entities.users.mapper;

import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.dto.CreateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UpdateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .oauthProviderId(user.getOauthProviderId())
                .provider(user.getProvider())
                .pictureId(user.getPicture() != null ? user.getPicture().getId() : null)
                .build();
    }

    public User toEntity(CreateUserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(dto.getPasswordHash())
                .phoneNumber(dto.getPhoneNumber())
                .role(dto.getRole())
                .oauthProviderId(dto.getOauthProviderId())
                .provider(dto.getProvider())
                .build();

        // Set picture if provided
        if (dto.getPictureId() != null) {
            Picture picture = new Picture();
            picture.setId(dto.getPictureId());
            user.setPicture(picture);
        }

        return user;
    }

    public void updateEntity(UpdateUserDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPasswordHash() != null) {
            user.setPasswordHash(dto.getPasswordHash());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getOauthProviderId() != null) {
            user.setOauthProviderId(dto.getOauthProviderId());
        }
        if (dto.getProvider() != null) {
            user.setProvider(dto.getProvider());
        }
        if (dto.getPictureId() != null) {
            Picture picture = new Picture();
            picture.setId(dto.getPictureId());
            user.setPicture(picture);
        } else if (dto.getPictureId() == null && user.getPicture() != null) {
            // If pictureId is explicitly set to null, remove the picture
            user.setPicture(null);
        }
    }
}

