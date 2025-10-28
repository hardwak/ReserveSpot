package com.pwr_zpi.reservespotapi.controller.restaurant_statistic;

import com.pwr_zpi.reservespotapi.dto.restaurant_statistic.CreateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.dto.restaurant_statistic.RestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.dto.restaurant_statistic.UpdateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.service.restaurant_statistic.RestaurantStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RestaurantStatisticController {

    private final RestaurantStatisticService statisticService;

    @GetMapping
    public ResponseEntity<List<RestaurantStatisticDto>> getAllStatistics() {
        List<RestaurantStatisticDto> statistics = statisticService.getAllStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantStatisticDto> getStatisticById(@PathVariable Long id) {
        Optional<RestaurantStatisticDto> statistic = statisticService.getStatisticById(id);
        return statistic.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<RestaurantStatisticDto>> getStatisticsByRestaurant(@PathVariable Long restaurantId) {
        List<RestaurantStatisticDto> statistics = statisticService.getStatisticsByRestaurantId(restaurantId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<RestaurantStatisticDto>> getStatisticsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<RestaurantStatisticDto> statistics = statisticService.getStatisticsByDate(date);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping
    public ResponseEntity<RestaurantStatisticDto> createStatistic(@Valid @RequestBody CreateRestaurantStatisticDto createDto) {
        RestaurantStatisticDto createdStatistic = statisticService.createStatistic(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStatistic);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantStatisticDto> updateStatistic(@PathVariable Long id, 
                                                                 @Valid @RequestBody UpdateRestaurantStatisticDto updateDto) {
        Optional<RestaurantStatisticDto> updatedStatistic = statisticService.updateStatistic(id, updateDto);
        return updatedStatistic.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatistic(@PathVariable Long id) {
        boolean deleted = statisticService.deleteStatistic(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getStatisticCount() {
        long count = statisticService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> statisticExists(@PathVariable Long id) {
        boolean exists = statisticService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
