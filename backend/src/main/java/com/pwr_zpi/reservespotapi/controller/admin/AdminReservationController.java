package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.mapper.ReservationMapper;
import com.pwr_zpi.reservespotapi.entities.reservation.service.ReservationService;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;

    @GetMapping
    public String listReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) ReservationStatus statusFilter,
            Model model) {
        
        List<Reservation> allReservations = reservationRepository.findAll();
        
        if (userId != null) {
            allReservations = allReservations.stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        }
        
        if (tableId != null) {
            allReservations = allReservations.stream()
                .filter(r -> r.getTable() != null && r.getTable().getId().equals(tableId))
                .collect(Collectors.toList());
        }
        
        if (statusFilter != null) {
            allReservations = allReservations.stream()
                .filter(r -> r.getStatus() == statusFilter)
                .collect(Collectors.toList());
        }
        
        allReservations.sort((r1, r2) -> {
            int result = 0;
            switch (sortBy) {
                case "reservationDatetime" -> result = r1.getReservationDatetime().compareTo(r2.getReservationDatetime());
                case "status" -> result = r1.getStatus().compareTo(r2.getStatus());
                default -> result = r1.getId().compareTo(r2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allReservations.size());
        List<Reservation> pageReservations = allReservations.subList(Math.min(start, allReservations.size()), end);
        
        List<ReservationDto> reservationDtos = pageReservations.stream()
            .map(reservationMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("reservations", reservationDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allReservations.size() / size));
        model.addAttribute("totalItems", allReservations.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("userId", userId);
        model.addAttribute("tableId", tableId);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("statuses", ReservationStatus.values());
        
        return "admin/reservations/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("reservation", new CreateReservationDto());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("tables", tableRepository.findAll());
        return "admin/reservations/create";
    }

    @PostMapping("/create")
    public String createReservation(@RequestParam Long userId,
                                   @Valid @ModelAttribute("reservation") CreateReservationDto createDto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("tables", tableRepository.findAll());
            return "admin/reservations/create";
        }
        
        try {
            reservationService.createReservation(createDto, userId);
            redirectAttributes.addFlashAttribute("success", "Reservation created successfully");
            return "redirect:/admin/reservations";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("tables", tableRepository.findAll());
            return "admin/reservations/create";
        }
    }

    @GetMapping("/{id}")
    public String viewReservation(@PathVariable Long id, Model model) {
        return reservationRepository.findById(id)
            .map(reservation -> {
                ReservationDto reservationDto = reservationMapper.toDto(reservation);
                model.addAttribute("reservation", reservationDto);
                return "admin/reservations/view";
            })
            .orElse("redirect:/admin/reservations");
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return reservationRepository.findById(id)
            .map(reservation -> {
                UpdateReservationDto updateDto = new UpdateReservationDto();
                updateDto.setReservationDatetime(reservation.getReservationDatetime());
                updateDto.setDurationMinutes(reservation.getDurationMinutes());
                updateDto.setStatus(reservation.getStatus());
                
                model.addAttribute("reservation", updateDto);
                model.addAttribute("reservationId", id);
                model.addAttribute("statuses", ReservationStatus.values());
                return "admin/reservations/edit";
            })
            .orElse("redirect:/admin/reservations");
    }

    @PostMapping("/{id}/edit")
    public String updateReservation(@PathVariable Long id,
                                  @Valid @ModelAttribute("reservation") UpdateReservationDto updateDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("reservationId", id);
            model.addAttribute("statuses", ReservationStatus.values());
            return "admin/reservations/edit";
        }
        
        try {
            reservationService.updateReservation(id, updateDto)
                .ifPresentOrElse(
                    reservationDto -> redirectAttributes.addFlashAttribute("success", "Reservation updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Reservation not found")
                );
            return "redirect:/admin/reservations/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reservationId", id);
            model.addAttribute("statuses", ReservationStatus.values());
            return "admin/reservations/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = reservationService.deleteReservation(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Reservation deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Reservation not found");
        }
        return "redirect:/admin/reservations";
    }
}

