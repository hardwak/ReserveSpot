package com.pwr_zpi.reservespotapi.entities.reservation.controller;

import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.service.ReservationService;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<ReservationDto> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable Long id) {
        Optional<ReservationDto> reservation = reservationService.getReservationById(id);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByTable(@PathVariable Long tableId) {
        List<ReservationDto> reservations = reservationService.getReservationsByTableId(tableId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationDto>> getReservationsByStatus(@PathVariable ReservationStatus status) {
        List<ReservationDto> reservations = reservationService.getReservationsByStatus(status);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ReservationDto>> getReservationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ReservationDto> reservations = reservationService.getReservationsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT', 'ADMIN')")
    public ResponseEntity<ReservationDto> createReservation(HttpServletRequest request,
                                                            @Valid @RequestBody CreateReservationDto createDto) {
        Long userId = currentUserService.requireCurrentUserId(request);
        ReservationDto createdReservation = reservationService.createReservation(createDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT', 'ADMIN')")
    public ResponseEntity<ReservationDto> updateReservation(@PathVariable Long id, 
                                                           @Valid @RequestBody UpdateReservationDto updateDto) {
        Optional<ReservationDto> updatedReservation = reservationService.updateReservation(id, updateDto);
        return updatedReservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT', 'ADMIN')")
    public ResponseEntity<Void> deleteReservation(HttpServletRequest request, @PathVariable Long id) {
        User currentUser = currentUserService.requireCurrentUser(request);

        switch (currentUser.getRole()) {
            case RESTAURANT -> reservationService.cancelReservationAsOwner(id, currentUser.getId());
            case CLIENT -> reservationService.cancelReservationAsUser(id, currentUser.getId());
            case ADMIN -> {
                boolean cancelled = reservationService.deleteReservation(id);
                if (!cancelled) {
                    return ResponseEntity.notFound().build();
                }
            }
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getReservationCount() {
        long count = reservationService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> reservationExists(@PathVariable Long id) {
        boolean exists = reservationService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/me/upcoming")
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT', 'ADMIN')")
    public ResponseEntity<List<ReservationDto>> getMyUpcomingReservations(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        List<ReservationDto> reservations = reservationService.getUpcomingReservationsForUser(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/me/history")
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT')")
    public ResponseEntity<List<ReservationDto>> getMyReservationHistory(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        List<ReservationDto> reservations = reservationService.getPastReservationsForUser(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/owner/upcoming")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<List<ReservationDto>> getUpcomingReservationsForOwner(HttpServletRequest request) {
        Long ownerId = currentUserService.requireCurrentUserId(request);
        List<ReservationDto> reservations = reservationService.getUpcomingReservationsForOwner(ownerId);
        return ResponseEntity.ok(reservations);
    }
}
