package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.mapper.RestaurantMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant.service.RestaurantService;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public String listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cityFilter,
            Model model) {
        
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allRestaurants = allRestaurants.stream()
                .filter(r -> (r.getName() != null && r.getName().toLowerCase().contains(searchLower)) ||
                            (r.getDescription() != null && r.getDescription().toLowerCase().contains(searchLower)) ||
                            (r.getAddress() != null && r.getAddress().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }
        
        if (cityFilter != null && !cityFilter.trim().isEmpty()) {
            allRestaurants = allRestaurants.stream()
                .filter(r -> r.getCity() != null && r.getCity().equalsIgnoreCase(cityFilter))
                .collect(Collectors.toList());
        }
        
        allRestaurants.sort((r1, r2) -> {
            int result = 0;
            switch (sortBy) {
                case "name" -> result = (r1.getName() != null ? r1.getName() : "").compareTo(r2.getName() != null ? r2.getName() : "");
                case "city" -> result = (r1.getCity() != null ? r1.getCity() : "").compareTo(r2.getCity() != null ? r2.getCity() : "");
                case "averageRating" -> result = Double.compare(r1.getAverageRating() != null ? r1.getAverageRating() : 0.0, 
                                                                 r2.getAverageRating() != null ? r2.getAverageRating() : 0.0);
                default -> result = r1.getId().compareTo(r2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allRestaurants.size());
        // Ensure start is within bounds
        start = Math.min(start, allRestaurants.size());
        List<Restaurant> pageRestaurants = (start < end) ? allRestaurants.subList(start, end) : List.of();
        
        List<RestaurantDto> restaurantDtos = pageRestaurants.stream()
            .map(restaurantMapper::toDto)
            .collect(Collectors.toList());
        
        List<String> cities = restaurantRepository.findAll().stream()
            .map(Restaurant::getCity)
            .filter(city -> city != null && !city.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        model.addAttribute("restaurants", restaurantDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allRestaurants.size() / size));
        model.addAttribute("totalItems", allRestaurants.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("cityFilter", cityFilter);
        model.addAttribute("cities", cities);
        
        return "admin/restaurants/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("restaurant", new CreateRestaurantDto());
        model.addAttribute("owners", userRepository.findAll().stream()
            .filter(u -> u.getRole() == com.pwr_zpi.reservespotapi.entities.users.Role.RESTAURANT)
            .collect(Collectors.toList()));
        model.addAttribute("tags", tagRepository.findAll());
        return "admin/restaurants/create";
    }

    @PostMapping("/create")
    public String createRestaurant(@Valid @ModelAttribute("restaurant") CreateRestaurantDto createDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("owners", userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.pwr_zpi.reservespotapi.entities.users.Role.RESTAURANT)
                .collect(Collectors.toList()));
            model.addAttribute("tags", tagRepository.findAll());
            return "admin/restaurants/create";
        }
        
        try {
            restaurantService.createRestaurant(createDto);
            redirectAttributes.addFlashAttribute("success", "Restaurant created successfully");
            return "redirect:/admin/restaurants/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("owners", userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.pwr_zpi.reservespotapi.entities.users.Role.RESTAURANT)
                .collect(Collectors.toList()));
            model.addAttribute("tags", tagRepository.findAll());
            return "admin/restaurants/create";
        }
    }

    @GetMapping("/{id}")
    public String viewRestaurant(@PathVariable Long id, Model model) {
        return restaurantRepository.findById(id)
            .map(restaurant -> {
                RestaurantDto restaurantDto = restaurantMapper.toDto(restaurant);
                model.addAttribute("restaurant", restaurantDto);
                return "admin/restaurants/view";
            })
            .orElse("redirect:/admin/restaurants");
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return restaurantRepository.findById(id)
            .map(restaurant -> {
                UpdateRestaurantDto updateDto = new UpdateRestaurantDto();
                updateDto.setName(restaurant.getName());
                updateDto.setAddress(restaurant.getAddress());
                updateDto.setCity(restaurant.getCity());
                updateDto.setDescription(restaurant.getDescription());
                updateDto.setOpeningHours(restaurant.getOpeningHours());
                updateDto.setLatitude(restaurant.getLatitude());
                updateDto.setLongitude(restaurant.getLongitude());
                updateDto.setPic(restaurant.getPic());
                
                model.addAttribute("restaurant", updateDto);
                model.addAttribute("restaurantId", id);
                model.addAttribute("owners", userRepository.findAll().stream()
                    .filter(u -> u.getRole() == com.pwr_zpi.reservespotapi.entities.users.Role.RESTAURANT)
                    .collect(Collectors.toList()));
                model.addAttribute("tags", tagRepository.findAll());
                model.addAttribute("currentTags", restaurant.getTags());
                return "admin/restaurants/edit";
            })
            .orElse("redirect:/admin/restaurants");
    }

    @PostMapping("/{id}/edit")
    public String updateRestaurant(@PathVariable Long id,
                                  @Valid @ModelAttribute("restaurant") UpdateRestaurantDto updateDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurantId", id);
            model.addAttribute("owners", userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.pwr_zpi.reservespotapi.entities.users.Role.RESTAURANT)
                .collect(Collectors.toList()));
            model.addAttribute("tags", tagRepository.findAll());
            return "admin/restaurants/edit";
        }
        
        try {
            restaurantService.updateRestaurant(id, updateDto)
                .ifPresentOrElse(
                    restaurantDto -> redirectAttributes.addFlashAttribute("success", "Restaurant updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Restaurant not found")
                );
            return "redirect:/admin/restaurants/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("restaurantId", id);
            model.addAttribute("owners", userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.pwr_zpi.reservespotapi.entities.users.Role.RESTAURANT)
                .collect(Collectors.toList()));
            model.addAttribute("tags", tagRepository.findAll());
            return "admin/restaurants/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteRestaurant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = restaurantService.deleteRestaurant(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Restaurant deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Restaurant not found");
        }
        return "redirect:/admin/restaurants";
    }
}

