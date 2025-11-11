package com.pwr_zpi.reservespotapi.entities.reservation.mapper;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.users.User;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationDto toDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return ReservationDto.builder()
                .id(reservation.getId())
                .userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
                .tableId(reservation.getTable() != null ? reservation.getTable().getId() : null)
                .reservationDatetime(reservation.getReservationDatetime())
                .durationMinutes(reservation.getDurationMinutes())
                .status(reservation.getStatus())
                .build();
    }

    public Reservation toEntity(CreateReservationDto dto, User user, RestaurantTable table, ReservationStatus status) {
        if (dto == null) {
            return null;
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .table(table)
                .reservationDatetime(dto.getReservationDatetime())
                .durationMinutes(dto.getDurationMinutes())
                .status(status)
                .build();

        return reservation;
    }

    public void updateEntity(UpdateReservationDto dto, Reservation reservation) {
        if (dto == null || reservation == null) {
            return;
        }

        if (dto.getReservationDatetime() != null) {
            reservation.setReservationDatetime(dto.getReservationDatetime());
        }
        if (dto.getDurationMinutes() != null) {
            reservation.setDurationMinutes(dto.getDurationMinutes());
        }
        if (dto.getStatus() != null) {
            reservation.setStatus(dto.getStatus());
        }
    }
}
