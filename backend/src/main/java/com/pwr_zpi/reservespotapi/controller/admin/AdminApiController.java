package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final TagRepository tagRepository;
    private final RestaurantTableRepository tableRepository;

    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@RequestParam String q) {
        List<User> users = userRepository.findAll().stream()
            .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(q.toLowerCase())) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(q.toLowerCase())))
            .limit(10)
            .collect(Collectors.toList());
        
        List<UserSearchResult> results = users.stream()
            .map(u -> new UserSearchResult(u.getId(), u.getName() + " (" + u.getEmail() + ")"))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/restaurants/search")
    public ResponseEntity<List<RestaurantSearchResult>> searchRestaurants(@RequestParam String q) {
        List<Restaurant> restaurants = restaurantRepository.findAll().stream()
            .filter(r -> (r.getName() != null && r.getName().toLowerCase().contains(q.toLowerCase())) ||
                        (r.getCity() != null && r.getCity().toLowerCase().contains(q.toLowerCase())))
            .limit(10)
            .collect(Collectors.toList());
        
        List<RestaurantSearchResult> results = restaurants.stream()
            .map(r -> new RestaurantSearchResult(r.getId(), r.getName() + " (" + (r.getCity() != null ? r.getCity() : "") + ")"))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/tags/search")
    public ResponseEntity<List<TagSearchResult>> searchTags(@RequestParam String q) {
        List<Tag> tags = tagRepository.findAll().stream()
            .filter(t -> t.getName() != null && t.getName().toLowerCase().contains(q.toLowerCase()))
            .limit(10)
            .collect(Collectors.toList());
        
        List<TagSearchResult> results = tags.stream()
            .map(t -> new TagSearchResult(t.getId(), t.getName()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/tables/search")
    public ResponseEntity<List<TableSearchResult>> searchTables(@RequestParam String q) {
        List<RestaurantTable> tables = tableRepository.findAll().stream()
            .filter(t -> (t.getTableNumber() != null && t.getTableNumber().toString().contains(q)) ||
                        (t.getRestaurant() != null && t.getRestaurant().getName() != null && 
                         t.getRestaurant().getName().toLowerCase().contains(q.toLowerCase())))
            .limit(10)
            .collect(Collectors.toList());
        
        List<TableSearchResult> results = tables.stream()
            .map(t -> new TableSearchResult(t.getId(), 
                "Table #" + (t.getTableNumber() != null ? t.getTableNumber() : t.getId()) + 
                " - " + (t.getRestaurant() != null ? t.getRestaurant().getName() : "")))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }

    // Search result DTOs
    public record UserSearchResult(Long id, String name) {}
    public record RestaurantSearchResult(Long id, String name) {}
    public record TagSearchResult(Long id, String name) {}
    public record TableSearchResult(Long id, String name) {}
}

