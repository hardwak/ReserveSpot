package com.pwr_zpi.reservespotapi.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TagDto {
    private Long id;
    private String name;
    private Set<Long> restaurantIds;
}
