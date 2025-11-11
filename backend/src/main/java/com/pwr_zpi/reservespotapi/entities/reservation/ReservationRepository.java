package com.pwr_zpi.reservespotapi.entities.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByTableId(Long tableId);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByReservationDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Reservation> findByUserIdAndReservationDatetimeGreaterThanEqualAndStatusNotOrderByReservationDatetimeAsc(
            Long userId,
            LocalDateTime cutoff,
            ReservationStatus excludedStatus
    );

    List<Reservation> findByUserIdAndReservationDatetimeLessThanOrderByReservationDatetimeDesc(
            Long userId,
            LocalDateTime cutoff
    );

    List<Reservation> findByUserIdAndStatusOrderByReservationDatetimeDesc(
            Long userId,
            ReservationStatus status
    );

    List<Reservation> findByTableRestaurantOwnerIdAndReservationDatetimeGreaterThanEqualAndStatusNotOrderByReservationDatetimeAsc(
            Long ownerId,
            LocalDateTime cutoff,
            ReservationStatus excludedStatus
    );

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    Optional<Reservation> findByIdAndTableRestaurantOwnerId(Long id, Long ownerId);

    boolean existsByUserIdAndTableRestaurantIdAndStatus(Long userId, Long restaurantId, ReservationStatus status);

    boolean existsByIdAndUserIdAndStatus(Long id, Long userId, ReservationStatus status);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reservation r
            WHERE r.user.id = :userId
              AND r.table.restaurant.id = :restaurantId
              AND r.status <> :excludedStatus
              AND r.reservationDatetime < :before
            """)
    boolean hasPastReservationForRestaurant(
            @Param("userId") Long userId,
            @Param("restaurantId") Long restaurantId,
            @Param("before") LocalDateTime before,
            @Param("excludedStatus") ReservationStatus excludedStatus
    );
}
