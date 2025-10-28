package com.pwr_zpi.reservespotapi.service.restaurant_table;

import com.pwr_zpi.reservespotapi.dto.restaurant_table.CreateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.dto.restaurant_table.RestaurantTableDto;
import com.pwr_zpi.reservespotapi.dto.restaurant_table.UpdateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.mapper.restaurant_table.RestaurantTableMapper;
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
        if (tableRepository.existsById(id)) {
            tableRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return tableRepository.existsById(id);
    }

    public long count() {
        return tableRepository.count();
    }
}
