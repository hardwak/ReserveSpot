package com.pwr_zpi.reservespotapi.entities.reservation.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.AvailableReservationSlotDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.mapper.ReservationMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final RestaurantRepository restaurantRepository;

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

    public List<AvailableReservationSlotDto> getAvailability(Long restaurantId, LocalDate date, int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be positive");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        Map<String, String> openingHours = restaurant.getOpeningHours();
        String dayKey = date.getDayOfWeek().name().toLowerCase();
        String rawSchedule = openingHours != null ? openingHours.get(dayKey) : null;

        if (rawSchedule == null || rawSchedule.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurant is closed on the selected day");
        }

        List<TimeWindow> windows = parseOpeningHours(rawSchedule, date);
        if (windows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid opening hours format");
        }

        List<RestaurantTable> tables = restaurantTableRepository.findByRestaurantId(restaurantId);
        if (tables.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurant has no tables configured");
        }

        List<AvailableReservationSlotDto> slots = new ArrayList<>();

        for (RestaurantTable table : tables) {
            for (TimeWindow window : windows) {
                List<Reservation> reservations = reservationRepository
                        .findByTableIdAndReservationDatetimeBetween(
                                table.getId(),
                                window.start(),
                                window.end())
                        .stream()
                        .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                        .sorted(Comparator.comparing(Reservation::getReservationDatetime))
                        .toList();

                slots.addAll(calculateSlotsForWindow(table, reservations, window, durationMinutes));
            }
        }

        return slots.stream()
                .sorted(Comparator.comparing(AvailableReservationSlotDto::getStart))
                .toList();
    }

    public ReservationDto createReservation(CreateReservationDto createDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        RestaurantTable table = restaurantTableRepository.findById(createDto.getTableId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));

        validateReservationTime(createDto.getReservationDatetime());
        int desiredDuration = normalizeDuration(createDto.getDurationMinutes());

        ensureSlotAvailable(table, createDto.getReservationDatetime(), desiredDuration);

        Reservation reservation = reservationMapper.toEntity(createDto, user, table, ReservationStatus.CONFIRMED);
        reservation.setDurationMinutes(desiredDuration);
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

    private List<TimeWindow> parseOpeningHours(String rawSchedule, LocalDate date) {
        List<TimeWindow> windows = new ArrayList<>();
        String[] segments = rawSchedule.split("[,;]");

        for (String segment : segments) {
            String trimmed = segment.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] parts = trimmed.split("-");
            if (parts.length != 2) {
                continue;
            }

            try {
                LocalTime start = LocalTime.parse(parts[0].trim());
                LocalTime end = LocalTime.parse(parts[1].trim());
                if (end.isAfter(start)) {
                    windows.add(new TimeWindow(date.atTime(start), date.atTime(end)));
                }
            } catch (Exception ignored) {
                // skip malformed segment
            }
        }

        return windows;
    }

    private List<AvailableReservationSlotDto> calculateSlotsForWindow(RestaurantTable table,
                                                                      List<Reservation> reservations,
                                                                      TimeWindow window,
                                                                      int durationMinutes) {
        List<AvailableReservationSlotDto> slots = new ArrayList<>();
        LocalDateTime cursor = window.start();

        for (Reservation reservation : reservations) {
            LocalDateTime reservationStart = reservation.getReservationDatetime();
            LocalDateTime reservationEnd = reservationStart.plusMinutes(
                    reservation.getDurationMinutes() != null && reservation.getDurationMinutes() > 0
                            ? reservation.getDurationMinutes()
                            : durationMinutes
            );

            if (reservationEnd.isBefore(cursor)) {
                continue;
            }

            if (reservationStart.isAfter(cursor)) {
                LocalDateTime gapEnd = reservationStart.isBefore(window.end()) ? reservationStart : window.end();
                addSlotsForGap(slots, table, cursor, gapEnd, durationMinutes);
            }

            if (reservationEnd.isAfter(cursor)) {
                cursor = reservationEnd.isAfter(window.end()) ? window.end() : reservationEnd;
            }

            if (!cursor.isBefore(window.end())) {
                break;
            }
        }

        if (cursor.isBefore(window.end())) {
            addSlotsForGap(slots, table, cursor, window.end(), durationMinutes);
        }

        return slots;
    }

    private void addSlotsForGap(List<AvailableReservationSlotDto> slots,
                                RestaurantTable table,
                                LocalDateTime gapStart,
                                LocalDateTime gapEnd,
                                int durationMinutes) {
        if (gapEnd.isBefore(gapStart.plusMinutes(durationMinutes))) {
            return;
        }

        LocalDateTime slotStart = gapStart;
        while (!slotStart.plusMinutes(durationMinutes).isAfter(gapEnd)) {
            slots.add(AvailableReservationSlotDto.builder()
                    .tableId(table.getId())
                    .tableCapacity(table.getCapacity())
                    .start(slotStart)
                    .end(slotStart.plusMinutes(durationMinutes))
                    .build());
            slotStart = slotStart.plusMinutes(durationMinutes);
        }
    }

    private void ensureSlotAvailable(RestaurantTable table, LocalDateTime desiredStart, int durationMinutes) {
        LocalDateTime dayStart = desiredStart.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        LocalDateTime desiredEnd = desiredStart.plusMinutes(durationMinutes);

        boolean hasConflict = reservationRepository
                .findByTableIdAndReservationDatetimeBetween(table.getId(), dayStart, dayEnd)
                .stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                .anyMatch(reservation -> overlaps(reservation, desiredStart, desiredEnd));

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected time slot is no longer available");
        }
    }

    private boolean overlaps(Reservation reservation, LocalDateTime desiredStart, LocalDateTime desiredEnd) {
        LocalDateTime reservationStart = reservation.getReservationDatetime();
        int duration = normalizeDuration(reservation.getDurationMinutes());
        LocalDateTime reservationEnd = reservationStart.plusMinutes(duration);

        return reservationStart.isBefore(desiredEnd) && reservationEnd.isAfter(desiredStart);
    }

    private int normalizeDuration(Integer durationMinutes) {
        return (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 60;
    }

    private record TimeWindow(LocalDateTime start, LocalDateTime end) { }
}
