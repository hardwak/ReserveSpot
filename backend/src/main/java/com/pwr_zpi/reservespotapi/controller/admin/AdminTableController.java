package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.CreateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.RestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.UpdateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.mapper.RestaurantTableMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tables")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTableController {

    private final RestaurantTableService tableService;
    private final RestaurantTableRepository tableRepository;
    private final RestaurantTableMapper tableMapper;
    private final RestaurantRepository restaurantRepository;

    @GetMapping("/list")
    public String listTables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) Integer minCapacity,
            Model model) {
        
        List<RestaurantTable> allTables = tableRepository.findAll();
        
        if (restaurantId != null) {
            allTables = allTables.stream()
                .filter(t -> t.getRestaurant() != null && t.getRestaurant().getId().equals(restaurantId))
                .collect(Collectors.toList());
        }
        
        if (minCapacity != null) {
            allTables = allTables.stream()
                .filter(t -> t.getCapacity() != null && t.getCapacity() >= minCapacity)
                .collect(Collectors.toList());
        }
        
        allTables.sort((t1, t2) -> {
            int result = 0;
            switch (sortBy) {
                case "tableNumber" -> result = Integer.compare(t1.getTableNumber() != null ? t1.getTableNumber() : 0, 
                                                               t2.getTableNumber() != null ? t2.getTableNumber() : 0);
                case "capacity" -> result = Integer.compare(t1.getCapacity() != null ? t1.getCapacity() : 0, 
                                                           t2.getCapacity() != null ? t2.getCapacity() : 0);
                default -> result = t1.getId().compareTo(t2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allTables.size());
        List<RestaurantTable> pageTables = allTables.subList(Math.min(start, allTables.size()), end);
        
        List<RestaurantTableDto> tableDtos = pageTables.stream()
            .map(tableMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("tables", tableDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allTables.size() / size));
        model.addAttribute("totalItems", allTables.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("minCapacity", minCapacity);
        
        return "admin/tables/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("table", new CreateRestaurantTableDto());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        return "admin/tables/create";
    }

    @PostMapping("/create")
    public String createTable(@Valid @ModelAttribute("table") CreateRestaurantTableDto createDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/tables/create";
        }
        
        try {
            tableService.createTable(createDto);
            redirectAttributes.addFlashAttribute("success", "Table created successfully");
            return "redirect:/admin/tables/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/tables/create";
        }
    }

    @GetMapping("/{id}")
    public String viewTable(@PathVariable Long id, Model model) {
        return tableRepository.findById(id)
            .map(table -> {
                RestaurantTableDto tableDto = tableMapper.toDto(table);
                model.addAttribute("table", tableDto);
                return "admin/tables/view";
            })
            .orElse("redirect:/admin/tables/list");
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model) {
        return tableRepository.findById(id)
            .map(table -> {
                UpdateRestaurantTableDto updateDto = new UpdateRestaurantTableDto();
                updateDto.setTableNumber(table.getTableNumber());
                updateDto.setCapacity(table.getCapacity());
                updateDto.setLocationInRestaurant(table.getLocationInRestaurant());
                
                model.addAttribute("table", updateDto);
                model.addAttribute("tableId", id);
                model.addAttribute("restaurants", restaurantRepository.findAll());
                return "admin/tables/edit";
            })
            .orElse("redirect:/admin/tables/list");
    }

    @PostMapping("/{id}/edit")
    public String updateTable(@PathVariable Long id,
                             @Valid @ModelAttribute("table") UpdateRestaurantTableDto updateDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("table", updateDto);
            model.addAttribute("tableId", id);
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/tables/edit";
        }
        
        try {
            tableService.updateTable(id, updateDto)
                .ifPresentOrElse(
                    tableDto -> redirectAttributes.addFlashAttribute("success", "Table updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Table not found")
                );
            return "redirect:/admin/tables/" + id;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            model.addAttribute("table", updateDto);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tableId", id);
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/tables/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = tableService.deleteTable(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Table deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Table not found");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            redirectAttributes.addFlashAttribute("error", "Could not delete table: " + e.getMessage());
        }
        return "redirect:/admin/tables/list";
    }
}

