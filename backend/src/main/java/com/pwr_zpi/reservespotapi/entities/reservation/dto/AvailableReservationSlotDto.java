package com.pwr_zpi.reservespotapi.entities.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableReservationSlotDto {
    private Long tableId;
    private Integer tableCapacity;
    private LocalDateTime start;
    private LocalDateTime end;
}

