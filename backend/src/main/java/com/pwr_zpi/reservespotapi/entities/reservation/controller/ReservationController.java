package com.pwr_zpi.reservespotapi.entities.reservation;

import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByUser(@PathVariable Long userId) {
        List<ReservationDto> reservations = reservationService.getReservationsByUserId(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByTable(@PathVariable Long tableId) {
        List<ReservationDto> reservations = reservationService.getReservationsByTableId(tableId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationDto>> getReservationsByStatus(@PathVariable String status) {
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
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT')")
    public ResponseEntity<ReservationDto> createReservation(@Valid @RequestBody CreateReservationDto createDto) {
        ReservationDto createdReservation = reservationService.createReservation(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT')")
    public ResponseEntity<ReservationDto> updateReservation(@PathVariable Long id, 
                                                           @Valid @RequestBody UpdateReservationDto updateDto) {
        Optional<ReservationDto> updatedReservation = reservationService.updateReservation(id, updateDto);
        return updatedReservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'RESTAURANT')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        boolean deleted = reservationService.deleteReservation(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
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
}
