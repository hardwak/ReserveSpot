package com.pwr_zpi.reservespotapi.entities.restaurant_table;

import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.CreateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.RestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.UpdateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    @GetMapping
    public ResponseEntity<List<RestaurantTableDto>> getAllTables() {
        List<RestaurantTableDto> tables = tableService.getAllTables();
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableDto> getTableById(@PathVariable Long id) {
        Optional<RestaurantTableDto> table = tableService.getTableById(id);
        return table.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<RestaurantTableDto>> getTablesByRestaurant(@PathVariable Long restaurantId) {
        List<RestaurantTableDto> tables = tableService.getTablesByRestaurantId(restaurantId);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/capacity/{capacity}")
    public ResponseEntity<List<RestaurantTableDto>> getTablesByCapacity(@PathVariable Integer capacity) {
        List<RestaurantTableDto> tables = tableService.getTablesByCapacity(capacity);
        return ResponseEntity.ok(tables);
    }

    @PostMapping
    public ResponseEntity<RestaurantTableDto> createTable(@Valid @RequestBody CreateRestaurantTableDto createDto) {
        RestaurantTableDto createdTable = tableService.createTable(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableDto> updateTable(@PathVariable Long id, 
                                                         @Valid @RequestBody UpdateRestaurantTableDto updateDto) {
        Optional<RestaurantTableDto> updatedTable = tableService.updateTable(id, updateDto);
        return updatedTable.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        boolean deleted = tableService.deleteTable(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTableCount() {
        long count = tableService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> tableExists(@PathVariable Long id) {
        boolean exists = tableService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
