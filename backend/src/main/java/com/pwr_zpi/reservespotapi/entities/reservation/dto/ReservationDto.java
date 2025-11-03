package com.pwr_zpi.reservespotapi.entities.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReservationDto {
    private Long id;
    private Long userId;
    private Long tableId;
    private LocalDateTime reservationDatetime;
    private Integer durationMinutes;
    private String status;
}
