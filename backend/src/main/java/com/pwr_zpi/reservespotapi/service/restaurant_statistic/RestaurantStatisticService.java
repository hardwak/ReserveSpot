package com.pwr_zpi.reservespotapi.service.restaurant_statistic;

import com.pwr_zpi.reservespotapi.dto.restaurant_statistic.CreateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.dto.restaurant_statistic.RestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.dto.restaurant_statistic.UpdateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatistic;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatisticRepository;
import com.pwr_zpi.reservespotapi.mapper.restaurant_statistic.RestaurantStatisticMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantStatisticService {

    private final RestaurantStatisticRepository statisticRepository;
    private final RestaurantStatisticMapper statisticMapper;

    public List<RestaurantStatisticDto> getAllStatistics() {
        return statisticRepository.findAll()
                .stream()
                .map(statisticMapper::toDto)
                .toList();
    }

    public Optional<RestaurantStatisticDto> getStatisticById(Long id) {
        return statisticRepository.findById(id)
                .map(statisticMapper::toDto);
    }

    public List<RestaurantStatisticDto> getStatisticsByRestaurantId(Long restaurantId) {
        return statisticRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(statisticMapper::toDto)
                .toList();
    }

    public List<RestaurantStatisticDto> getStatisticsByDate(LocalDate date) {
        return statisticRepository.findByDate(date)
                .stream()
                .map(statisticMapper::toDto)
                .toList();
    }

    public RestaurantStatisticDto createStatistic(CreateRestaurantStatisticDto createDto) {
        RestaurantStatistic statistic = statisticMapper.toEntity(createDto);
        RestaurantStatistic savedStatistic = statisticRepository.save(statistic);
        return statisticMapper.toDto(savedStatistic);
    }

    public Optional<RestaurantStatisticDto> updateStatistic(Long id, UpdateRestaurantStatisticDto updateDto) {
        return statisticRepository.findById(id)
                .map(statistic -> {
                    statisticMapper.updateEntity(updateDto, statistic);
                    RestaurantStatistic savedStatistic = statisticRepository.save(statistic);
                    return statisticMapper.toDto(savedStatistic);
                });
    }

    public boolean deleteStatistic(Long id) {
        if (statisticRepository.existsById(id)) {
            statisticRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return statisticRepository.existsById(id);
    }

    public long count() {
        return statisticRepository.count();
    }
}
