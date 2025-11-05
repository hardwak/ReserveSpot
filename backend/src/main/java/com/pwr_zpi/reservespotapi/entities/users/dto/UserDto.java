package com.pwr_zpi.reservespotapi.entities.users.dto;

import com.pwr_zpi.reservespotapi.entities.users.AuthProvider;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private String oauthProviderId;
    private AuthProvider provider;
    private Long pictureId;
}

