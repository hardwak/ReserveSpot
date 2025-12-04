package com.pwr_zpi.reservespotapi.entities.reservation.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.AvailableReservationSlotDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    private User testUser;
    private User restaurantOwner;
    private Restaurant testRestaurant;
    private RestaurantTable testTable;

    @BeforeEach
    void setUp() {
        // Clean up
        reservationRepository.deleteAll();
        restaurantTableRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("password")
                .role(Role.CLIENT)
                .name("Test User")
                .build();
        testUser = userRepository.save(testUser);

        // Create restaurant owner
        restaurantOwner = User.builder()
                .email("owner@restaurant.com")
                .passwordHash("password")
                .role(Role.RESTAURANT)
                .name("Restaurant Owner")
                .build();
        restaurantOwner = userRepository.save(restaurantOwner);

        // Create test restaurant with opening hours
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");
        openingHours.put("tuesday", "10:00-22:00");
        openingHours.put("wednesday", "10:00-22:00");
        openingHours.put("thursday", "10:00-22:00");
        openingHours.put("friday", "10:00-22:00");
        openingHours.put("saturday", "10:00-22:00");
        openingHours.put("sunday", "10:00-22:00");

        testRestaurant = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Test Restaurant")
                .address("123 Test St")
                .city("Test City")
                .description("A test restaurant")
                .openingHours(openingHours)
                .averageRating(4.5)
                .build();
        testRestaurant = restaurantRepository.save(testRestaurant);

        // Create test table
        testTable = RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber(1)
                .capacity(4)
                .locationInRestaurant("Window")
                .build();
        testTable = restaurantTableRepository.save(testTable);
    }

    @Test
    void testCreateReservation_Success() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();

        // When
        ReservationDto result = reservationService.createReservation(createDto, testUser.getId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testTable.getId(), result.getTableId());
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        assertEquals(90, result.getDurationMinutes());
    }

    @Test
    void testCreateReservation_PastDateTime_ThrowsException() {
        // Given
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(pastDateTime)
                .durationMinutes(90)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.createReservation(createDto, testUser.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testCreateReservation_OverlappingReservation_ThrowsException() {
        // Given
        LocalDateTime reservationTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        
        // Create first reservation
        CreateReservationDto firstDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(reservationTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(firstDto, testUser.getId());

        // Create overlapping reservation
        CreateReservationDto overlappingDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(reservationTime.plusMinutes(30)) // Overlaps with first
                .durationMinutes(60)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.createReservation(overlappingDto, testUser.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetAvailability_Success() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        int durationMinutes = 60;

        // When
        List<AvailableReservationSlotDto> slots = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, durationMinutes);

        // Then
        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertTrue(slots.stream().anyMatch(slot -> slot.getTableId().equals(testTable.getId())));
    }

    @Test
    void testGetAvailability_RestaurantClosed_ThrowsException() {
        // Given
        Map<String, String> closedHours = new HashMap<>();
        closedHours.put("monday", "closed");
        testRestaurant.setOpeningHours(closedHours);
        restaurantRepository.save(testRestaurant);

        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        final LocalDate finalMonday = monday.isBefore(LocalDate.now()) ? monday.plusWeeks(1) : monday;

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.getAvailability(testRestaurant.getId(), finalMonday, 60));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testUpdateReservation_Success() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        UpdateReservationDto updateDto = UpdateReservationDto.builder()
                .durationMinutes(120)
                .build();

        // When
        Optional<ReservationDto> updated = reservationService.updateReservation(created.getId(), updateDto);

        // Then
        assertTrue(updated.isPresent());
        assertEquals(120, updated.get().getDurationMinutes());
    }

    @Test
    void testCancelReservationAsUser_Success() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        // When
        ReservationDto cancelled = reservationService.cancelReservationAsUser(created.getId(), testUser.getId());

        // Then
        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testCancelReservationAsUser_PastReservation_ThrowsException() {
        // Given
        Reservation pastReservation = Reservation.builder()
                .user(testUser)
                .table(testTable)
                .reservationDatetime(LocalDateTime.now().minusDays(1))
                .durationMinutes(90)
                .status(ReservationStatus.CONFIRMED)
                .build();
        final Reservation savedPastReservation = reservationRepository.save(pastReservation);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.cancelReservationAsUser(savedPastReservation.getId(), testUser.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetUpcomingReservationsForUser() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        List<ReservationDto> upcoming = reservationService.getUpcomingReservationsForUser(testUser.getId());

        // Then
        assertNotNull(upcoming);
        assertFalse(upcoming.isEmpty());
        assertTrue(upcoming.stream().allMatch(r -> r.getReservationDatetime().isAfter(LocalDateTime.now())));
    }

    @Test
    void testGetPastReservationsForUser() {
        // Given
        Reservation pastReservation = Reservation.builder()
                .user(testUser)
                .table(testTable)
                .reservationDatetime(LocalDateTime.now().minusDays(1))
                .durationMinutes(90)
                .status(ReservationStatus.COMPLETED)
                .build();
        reservationRepository.save(pastReservation);

        // When
        List<ReservationDto> past = reservationService.getPastReservationsForUser(testUser.getId());

        // Then
        assertNotNull(past);
        assertFalse(past.isEmpty());
    }

    @Test
    void testGetReservationsByStatus() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        List<ReservationDto> confirmed = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMED);

        // Then
        assertNotNull(confirmed);
        assertTrue(confirmed.stream().allMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED));
    }

    @Test
    void testCreateReservation_DefaultDuration() {
        // Given - reservation without duration should default to 60 minutes
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(null) // No duration specified
                .build();

        // When
        ReservationDto result = reservationService.createReservation(createDto, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(60, result.getDurationMinutes()); // Should default to 60
    }

    @Test
    void testCreateReservation_AdjacentReservations_Allowed() {
        // Given
        LocalDateTime firstReservationTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        
        // Create first reservation
        CreateReservationDto firstDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(firstReservationTime)
                .durationMinutes(60)
                .build();
        reservationService.createReservation(firstDto, testUser.getId());

        // Create adjacent reservation (starts exactly when first ends)
        CreateReservationDto adjacentDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(firstReservationTime.plusMinutes(60)) // Starts when first ends
                .durationMinutes(60)
                .build();

        // When
        ReservationDto result = reservationService.createReservation(adjacentDto, testUser.getId());

        // Then - should succeed as they don't overlap
        assertNotNull(result);
        assertEquals(firstReservationTime.plusMinutes(60), result.getReservationDatetime());
    }

    @Test
    void testCreateReservation_UserNotFound_ThrowsException() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.createReservation(createDto, 999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testCreateReservation_TableNotFound_ThrowsException() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(999L) // Non-existent table
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.createReservation(createDto, testUser.getId()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetAvailability_MultipleTables() {
        // Given
        RestaurantTable table2 = RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber(2)
                .capacity(6)
                .locationInRestaurant("Main Hall")
                .build();
        final RestaurantTable savedTable2 = restaurantTableRepository.save(table2);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        int durationMinutes = 60;

        // When
        List<AvailableReservationSlotDto> slots = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, durationMinutes);

        // Then
        assertNotNull(slots);
        assertTrue(slots.size() >= 2); // Should have slots for both tables
        assertTrue(slots.stream().anyMatch(slot -> slot.getTableId().equals(testTable.getId())));
        assertTrue(slots.stream().anyMatch(slot -> slot.getTableId().equals(savedTable2.getId())));
    }

    @Test
    void testGetAvailability_WithExistingReservation() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime reservationTime = tomorrow.atTime(19, 0);
        
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(reservationTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        List<AvailableReservationSlotDto> slots = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, 60);

        // Then
        assertNotNull(slots);
        // Should not have slots during the reserved time (19:00-20:30)
        assertTrue(slots.stream().noneMatch(slot -> 
            slot.getStart().equals(reservationTime) || 
            (slot.getStart().isAfter(reservationTime) && slot.getStart().isBefore(reservationTime.plusMinutes(90)))
        ));
    }

    @Test
    void testGetAvailability_InvalidDuration_ThrowsException() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.getAvailability(testRestaurant.getId(), tomorrow, 0));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetAvailability_RestaurantNotFound_ThrowsException() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.getAvailability(999L, tomorrow, 60));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetAvailability_NoTables_ThrowsException() {
        // Given
        Restaurant restaurantWithoutTables = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant Without Tables")
                .city("Test City")
                .openingHours(testRestaurant.getOpeningHours())
                .build();
        final Restaurant savedRestaurantWithoutTables = restaurantRepository.save(restaurantWithoutTables);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.getAvailability(savedRestaurantWithoutTables.getId(), tomorrow, 60));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testGetReservationsByTableId() {
        // Given
        RestaurantTable table2 = RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber(2)
                .capacity(4)
                .build();
        table2 = restaurantTableRepository.save(table2);

        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        List<ReservationDto> reservations = reservationService.getReservationsByTableId(testTable.getId());

        // Then
        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        assertTrue(reservations.stream().allMatch(r -> r.getTableId().equals(testTable.getId())));
    }

    @Test
    void testGetReservationsByDateRange() {
        // Given
        LocalDateTime today = LocalDateTime.now().plusHours(2);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).plusHours(2);
        LocalDateTime dayAfter = LocalDateTime.now().plusDays(2).plusHours(2);

        CreateReservationDto dto1 = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(today)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(dto1, testUser.getId());

        CreateReservationDto dto2 = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(tomorrow)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(dto2, testUser.getId());

        CreateReservationDto dto3 = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(dayAfter)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(dto3, testUser.getId());

        // When
        LocalDateTime startDate = LocalDateTime.now().plusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1).plusHours(12);
        List<ReservationDto> reservations = reservationService.getReservationsByDateRange(startDate, endDate);

        // Then
        assertNotNull(reservations);
        assertTrue(reservations.size() >= 2); // Should include today and tomorrow
        assertTrue(reservations.stream().allMatch(r -> 
            !r.getReservationDatetime().isBefore(startDate) && 
            !r.getReservationDatetime().isAfter(endDate)
        ));
    }

    @Test
    void testCancelReservationAsOwner_Success() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        // When
        ReservationDto cancelled = reservationService.cancelReservationAsOwner(created.getId(), restaurantOwner.getId());

        // Then
        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testCancelReservationAsOwner_AlreadyCancelled() {
        // Given
        Reservation cancelledReservation = Reservation.builder()
                .user(testUser)
                .table(testTable)
                .reservationDatetime(LocalDateTime.now().plusDays(1))
                .durationMinutes(90)
                .status(ReservationStatus.CANCELLED)
                .build();
        cancelledReservation = reservationRepository.save(cancelledReservation);

        // When
        ReservationDto result = reservationService.cancelReservationAsOwner(
                cancelledReservation.getId(), restaurantOwner.getId());

        // Then - should return the already cancelled reservation
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelReservationAsOwner_WrongOwner_ThrowsException() {
        // Given
        User anotherOwner = User.builder()
                .email("another@owner.com")
                .passwordHash("password")
                .role(Role.RESTAURANT)
                .build();
        final User savedAnotherOwner = userRepository.save(anotherOwner);

        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.cancelReservationAsOwner(created.getId(), savedAnotherOwner.getId()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testCancelReservationAsUser_WrongUser_ThrowsException() {
        // Given
        User anotherUser = User.builder()
                .email("another@user.com")
                .passwordHash("password")
                .role(Role.CLIENT)
                .build();
        final User savedAnotherUser = userRepository.save(anotherUser);

        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.cancelReservationAsUser(created.getId(), savedAnotherUser.getId()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testDeleteReservation_Success() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        // When
        boolean deleted = reservationService.deleteReservation(created.getId());

        // Then
        assertTrue(deleted);
        Optional<ReservationDto> reservation = reservationService.getReservationById(created.getId());
        assertTrue(reservation.isPresent());
        assertEquals(ReservationStatus.CANCELLED, reservation.get().getStatus());
    }

    @Test
    void testDeleteReservation_NotFound() {
        // When
        boolean deleted = reservationService.deleteReservation(999L);

        // Then
        assertFalse(deleted);
    }

    @Test
    void testExistsById_True() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        ReservationDto created = reservationService.createReservation(createDto, testUser.getId());

        // When
        boolean exists = reservationService.existsById(created.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsById_False() {
        // When
        boolean exists = reservationService.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void testCount() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        long count = reservationService.count();

        // Then
        assertTrue(count >= 1);
    }

    @Test
    void testGetUpcomingReservationsForOwner() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        List<ReservationDto> upcoming = reservationService.getUpcomingReservationsForOwner(restaurantOwner.getId());

        // Then
        assertNotNull(upcoming);
        assertFalse(upcoming.isEmpty());
        assertTrue(upcoming.stream().allMatch(r -> r.getReservationDatetime().isAfter(LocalDateTime.now())));
    }

    @Test
    void testGetAllReservations() {
        // Given
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        CreateReservationDto createDto = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(futureDateTime)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(createDto, testUser.getId());

        // When
        List<ReservationDto> all = reservationService.getAllReservations();

        // Then
        assertNotNull(all);
        assertTrue(all.size() >= 1);
    }

    @Test
    void testGetReservationById_NotFound() {
        // When
        Optional<ReservationDto> result = reservationService.getReservationById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateReservation_MultipleUsers_SameTable() {
        // Given
        User user2 = User.builder()
                .email("user2@example.com")
                .passwordHash("password")
                .role(Role.CLIENT)
                .name("User 2")
                .build();
        user2 = userRepository.save(user2);

        LocalDateTime time1 = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        LocalDateTime time2 = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0);

        CreateReservationDto dto1 = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(time1)
                .durationMinutes(90)
                .build();
        reservationService.createReservation(dto1, testUser.getId());

        CreateReservationDto dto2 = CreateReservationDto.builder()
                .tableId(testTable.getId())
                .reservationDatetime(time2)
                .durationMinutes(60)
                .build();

        // When
        ReservationDto result = reservationService.createReservation(dto2, user2.getId());

        // Then - should succeed as times don't overlap
        assertNotNull(result);
        assertEquals(user2.getId(), result.getUserId());
    }

    @Test
    void testUpdateReservation_NotFound() {
        // Given
        UpdateReservationDto updateDto = UpdateReservationDto.builder()
                .durationMinutes(120)
                .build();

        // When
        Optional<ReservationDto> result = reservationService.updateReservation(999L, updateDto);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAvailability_DifferentDurations() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // When - test with different duration requirements
        List<AvailableReservationSlotDto> slots30 = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, 30);
        List<AvailableReservationSlotDto> slots60 = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, 60);
        List<AvailableReservationSlotDto> slots120 = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, 120);

        // Then
        assertNotNull(slots30);
        assertNotNull(slots60);
        assertNotNull(slots120);
        // Longer duration should have fewer or equal slots
        assertTrue(slots30.size() >= slots60.size());
        assertTrue(slots60.size() >= slots120.size());
    }

    @Test
    void testGetAvailability_ExcludesCancelledReservations() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime reservationTime = tomorrow.atTime(19, 0);
        
        Reservation cancelledReservation = Reservation.builder()
                .user(testUser)
                .table(testTable)
                .reservationDatetime(reservationTime)
                .durationMinutes(90)
                .status(ReservationStatus.CANCELLED)
                .build();
        reservationRepository.save(cancelledReservation);

        // When
        List<AvailableReservationSlotDto> slots = reservationService.getAvailability(
                testRestaurant.getId(), tomorrow, 60);

        // Then - cancelled reservations should not block availability
        assertNotNull(slots);
        // Should have slots available at the cancelled reservation time
        assertTrue(slots.stream().anyMatch(slot -> 
            slot.getStart().equals(reservationTime) || 
            slot.getStart().isAfter(reservationTime)
        ));
    }
}

