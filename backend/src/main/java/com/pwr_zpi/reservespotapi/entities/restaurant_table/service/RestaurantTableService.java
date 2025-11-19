package com.pwr_zpi.reservespotapi.entities.restaurant_table.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.CreateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.RestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.UpdateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.mapper.RestaurantTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final RestaurantTableMapper tableMapper;
    private final ReservationRepository reservationRepository;

    public List<RestaurantTableDto> getAllTables() {
        return tableRepository.findAll()
                .stream()
                .map(tableMapper::toDto)
                .toList();
    }

    public Optional<RestaurantTableDto> getTableById(Long id) {
        return tableRepository.findById(id)
                .map(tableMapper::toDto);
    }

    public List<RestaurantTableDto> getTablesByRestaurantId(Long restaurantId) {
        return tableRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(tableMapper::toDto)
                .toList();
    }

    public List<RestaurantTableDto> getTablesByCapacity(Integer capacity) {
        return tableRepository.findByCapacity(capacity)
                .stream()
                .map(tableMapper::toDto)
                .toList();
    }

    public RestaurantTableDto createTable(CreateRestaurantTableDto createDto) {
        RestaurantTable table = tableMapper.toEntity(createDto);
        RestaurantTable savedTable = tableRepository.save(table);
        return tableMapper.toDto(savedTable);
    }

    public Optional<RestaurantTableDto> updateTable(Long id, UpdateRestaurantTableDto updateDto) {
        return tableRepository.findById(id)
                .map(table -> {
                    tableMapper.updateEntity(updateDto, table);
                    RestaurantTable savedTable = tableRepository.save(table);
                    return tableMapper.toDto(savedTable);
                });
    }

    public boolean deleteTable(Long id) {
        return tableRepository.findById(id)
            .map(table -> {
                // Delete all reservations for this table first
                List<Reservation> reservations = reservationRepository.findByTableId(id);
                if (!reservations.isEmpty()) {
                    reservationRepository.deleteAll(reservations);
                }
                // Now delete the table
                tableRepository.delete(table);
            return true;
            })
            .orElse(false);
    }

    public boolean existsById(Long id) {
        return tableRepository.existsById(id);
    }

    public long count() {
        return tableRepository.count();
    }
}
