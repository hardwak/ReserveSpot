package com.pwr_zpi.reservespotapi.entities.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByTableId(Long tableId);
    List<Reservation> findByStatus(String status);
    List<Reservation> findByReservationDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}
