package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatistic;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatisticRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto.CreateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto.RestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto.UpdateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.mapper.RestaurantStatisticMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.service.RestaurantStatisticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticController {

    private final RestaurantStatisticService statisticService;
    private final RestaurantStatisticRepository statisticRepository;
    private final RestaurantStatisticMapper statisticMapper;
    private final RestaurantRepository restaurantRepository;

    @GetMapping("/list")
    public String listStatistics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) LocalDate dateFilter,
            Model model) {
        
        List<RestaurantStatistic> allStatistics = statisticRepository.findAll();
        
        if (restaurantId != null) {
            allStatistics = allStatistics.stream()
                .filter(s -> s.getRestaurant() != null && s.getRestaurant().getId().equals(restaurantId))
                .collect(Collectors.toList());
        }
        
        if (dateFilter != null) {
            allStatistics = allStatistics.stream()
                .filter(s -> s.getDate() != null && s.getDate().equals(dateFilter))
                .collect(Collectors.toList());
        }
        
        allStatistics.sort((s1, s2) -> {
            int result = 0;
            switch (sortBy) {
                case "date" -> result = (s1.getDate() != null ? s1.getDate() : LocalDate.MIN)
                    .compareTo(s2.getDate() != null ? s2.getDate() : LocalDate.MIN);
                case "hourOfDay" -> result = Integer.compare(s1.getHourOfDay() != null ? s1.getHourOfDay() : 0, 
                                                            s2.getHourOfDay() != null ? s2.getHourOfDay() : 0);
                default -> result = s1.getId().compareTo(s2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allStatistics.size());
        List<RestaurantStatistic> pageStatistics = allStatistics.subList(Math.min(start, allStatistics.size()), end);
        
        List<RestaurantStatisticDto> statisticDtos = pageStatistics.stream()
            .map(statisticMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("statistics", statisticDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allStatistics.size() / size));
        model.addAttribute("totalItems", allStatistics.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("dateFilter", dateFilter);
        
        return "admin/statistics/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("statistic", new CreateRestaurantStatisticDto());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        return "admin/statistics/create";
    }

    @PostMapping("/create")
    public String createStatistic(@Valid @ModelAttribute("statistic") CreateRestaurantStatisticDto createDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/statistics/create";
        }
        
        try {
            statisticService.createStatistic(createDto);
            redirectAttributes.addFlashAttribute("success", "Statistic created successfully");
            return "redirect:/admin/statistics/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/statistics/create";
        }
    }

    @GetMapping("/{id}")
    public String viewStatistic(@PathVariable Long id, Model model) {
        return statisticRepository.findById(id)
            .map(statistic -> {
                RestaurantStatisticDto statisticDto = statisticMapper.toDto(statistic);
                model.addAttribute("statistic", statisticDto);
                return "admin/statistics/view";
            })
            .orElse("redirect:/admin/statistics/list");
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return statisticRepository.findById(id)
            .map(statistic -> {
                UpdateRestaurantStatisticDto updateDto = new UpdateRestaurantStatisticDto();
                updateDto.setHourOfDay(statistic.getHourOfDay());
                updateDto.setAverageOccupancy(statistic.getAverageOccupancy());
                updateDto.setDate(statistic.getDate());
                
                model.addAttribute("statistic", updateDto);
                model.addAttribute("statisticId", id);
                model.addAttribute("restaurants", restaurantRepository.findAll());
                return "admin/statistics/edit";
            })
            .orElse("redirect:/admin/statistics/list");
    }

    @PostMapping("/{id}/edit")
    public String updateStatistic(@PathVariable Long id,
                                 @Valid @ModelAttribute("statistic") UpdateRestaurantStatisticDto updateDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statisticId", id);
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/statistics/edit";
        }
        
        try {
            statisticService.updateStatistic(id, updateDto)
                .ifPresentOrElse(
                    statisticDto -> redirectAttributes.addFlashAttribute("success", "Statistic updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Statistic not found")
                );
            return "redirect:/admin/statistics/list/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statisticId", id);
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/statistics/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteStatistic(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = statisticService.deleteStatistic(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Statistic deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Statistic not found");
        }
        return "redirect:/admin/statistics/list";
    }
}

