package com.pwr_zpi.reservespotapi.mapper.reservation;

import com.pwr_zpi.reservespotapi.dto.reservation.CreateReservationDto;
import com.pwr_zpi.reservespotapi.dto.reservation.ReservationDto;
import com.pwr_zpi.reservespotapi.dto.reservation.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
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

    public Reservation toEntity(CreateReservationDto dto) {
        if (dto == null) {
            return null;
        }

        Reservation reservation = Reservation.builder()
                .reservationDatetime(dto.getReservationDatetime())
                .durationMinutes(dto.getDurationMinutes())
                .status(dto.getStatus())
                .build();

        // Set user if provided
        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId());
            reservation.setUser(user);
        }

        // Set table if provided
        if (dto.getTableId() != null) {
            RestaurantTable table = new RestaurantTable();
            table.setId(dto.getTableId());
            reservation.setTable(table);
        }

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
