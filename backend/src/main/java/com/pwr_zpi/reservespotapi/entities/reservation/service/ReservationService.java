package com.pwr_zpi.reservespotapi.entities.reservation.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.mapper.ReservationMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final UserRepository userRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public Optional<ReservationDto> getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDto);
    }

    public List<ReservationDto> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getReservationsByTableId(Long tableId) {
        return reservationRepository.findByTableId(tableId)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByReservationDatetimeBetween(startDate, endDate)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public ReservationDto createReservation(CreateReservationDto createDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        RestaurantTable table = restaurantTableRepository.findById(createDto.getTableId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));

        validateReservationTime(createDto.getReservationDatetime());
        if (createDto.getDurationMinutes() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration is required");
        }

        Reservation reservation = reservationMapper.toEntity(createDto, user, table, ReservationStatus.CONFIRMED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(savedReservation);
    }

    public Optional<ReservationDto> updateReservation(Long id, UpdateReservationDto updateDto) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservationMapper.updateEntity(updateDto, reservation);
                    Reservation savedReservation = reservationRepository.save(reservation);
                    return reservationMapper.toDto(savedReservation);
                });
    }

    public List<ReservationDto> getUpcomingReservationsForUser(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository
                .findByUserIdAndReservationDatetimeGreaterThanEqualAndStatusNotOrderByReservationDatetimeAsc(
                        userId,
                        now,
                        ReservationStatus.CANCELLED
                )
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getPastReservationsForUser(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<ReservationDto> history = reservationRepository
                .findByUserIdAndReservationDatetimeLessThanOrderByReservationDatetimeDesc(userId, now)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
        List<ReservationDto> cancelled = reservationRepository
                .findByUserIdAndStatusOrderByReservationDatetimeDesc(userId, ReservationStatus.CANCELLED)
                .stream()
                .map(reservationMapper::toDto)
                .toList();

        Map<Long, ReservationDto> merged = new LinkedHashMap<>();
        history.forEach(dto -> merged.put(dto.getId(), dto));
        cancelled.forEach(dto -> merged.putIfAbsent(dto.getId(), dto));

        return new ArrayList<>(merged.values());
    }

    public List<ReservationDto> getUpcomingReservationsForOwner(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository
                .findByTableRestaurantOwnerIdAndReservationDatetimeGreaterThanEqualAndStatusNotOrderByReservationDatetimeAsc(
                        ownerId,
                        now,
                        ReservationStatus.CANCELLED
                )
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public ReservationDto cancelReservationAsUser(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return reservationMapper.toDto(reservation);
        }

        if (reservation.getReservationDatetime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel past reservations");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toDto(reservationRepository.save(reservation));
    }

    public ReservationDto cancelReservationAsOwner(Long reservationId, Long ownerId) {
        Reservation reservation = reservationRepository.findByIdAndTableRestaurantOwnerId(reservationId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return reservationMapper.toDto(reservation);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toDto(reservationRepository.save(reservation));
    }

    public boolean deleteReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.CANCELLED) {
                        reservation.setStatus(ReservationStatus.CANCELLED);
                        reservationRepository.save(reservation);
                    }
                    return true;
                })
                .orElse(false);
    }

    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    public long count() {
        return reservationRepository.count();
    }

    private void validateReservationTime(LocalDateTime reservationDatetime) {
        if (reservationDatetime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation datetime is required");
        }

        if (!reservationDatetime.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation datetime must be in the future");
        }
    }
}
