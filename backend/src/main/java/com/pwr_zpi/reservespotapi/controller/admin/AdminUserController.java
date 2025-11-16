package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import com.pwr_zpi.reservespotapi.entities.users.dto.CreateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UpdateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UserDto;
import com.pwr_zpi.reservespotapi.entities.users.mapper.UserMapper;
import com.pwr_zpi.reservespotapi.entities.users.service.UserService;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role roleFilter,
            Model model) {
        
        // For now, fetch all and filter in memory (can be optimized with JPA queries later)
        // Initialize picture relationships to avoid lazy loading issues
        List<User> allUsers = userRepository.findAll();
        // Force initialization of picture relationships
        allUsers.forEach(user -> {
            if (user.getPicture() != null) {
                user.getPicture().getId(); // Force initialization
            }
        });
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allUsers = allUsers.stream()
                .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(searchLower)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower)) ||
                            (u.getPhoneNumber() != null && u.getPhoneNumber().contains(search)))
                .collect(Collectors.toList());
        }
        
        if (roleFilter != null) {
            allUsers = allUsers.stream()
                .filter(u -> u.getRole() == roleFilter)
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        allUsers.sort((u1, u2) -> {
            int result = 0;
            switch (sortBy) {
                case "name" -> result = (u1.getName() != null ? u1.getName() : "").compareTo(u2.getName() != null ? u2.getName() : "");
                case "email" -> result = u1.getEmail().compareTo(u2.getEmail());
                case "role" -> result = u1.getRole().compareTo(u2.getRole());
                default -> result = u1.getId().compareTo(u2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        // Paginate
        int start = page * size;
        int end = Math.min(start + size, allUsers.size());
        // Ensure start is within bounds
        start = Math.min(start, allUsers.size());
        List<User> pageUsers = (start < end) ? allUsers.subList(start, end) : List.of();
        
        List<UserDto> userDtos = pageUsers.stream()
            .map(userMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("users", userDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allUsers.size() / size));
        model.addAttribute("totalItems", allUsers.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("roleFilter", roleFilter);
        model.addAttribute("roles", Role.values());
        
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new CreateUserDto());
        model.addAttribute("roles", Role.values());
        model.addAttribute("providers", com.pwr_zpi.reservespotapi.entities.users.AuthProvider.values());
        return "admin/users/create";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("user") CreateUserDto createDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("providers", com.pwr_zpi.reservespotapi.entities.users.AuthProvider.values());
            return "admin/users/create";
        }
        
        try {
            userService.createUser(createDto);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
            return "redirect:/admin/users/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("providers", com.pwr_zpi.reservespotapi.entities.users.AuthProvider.values());
            return "admin/users/create";
        }
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        return userRepository.findById(id)
            .map(user -> {
                UserDto userDto = userMapper.toDto(user);
                model.addAttribute("user", userDto);
                return "admin/users/view";
            })
            .orElse("redirect:/admin/users");
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return userRepository.findById(id)
            .map(user -> {
                UpdateUserDto updateDto = new UpdateUserDto();
                updateDto.setName(user.getName());
                updateDto.setEmail(user.getEmail());
                updateDto.setPhoneNumber(user.getPhoneNumber());
                updateDto.setRole(user.getRole());
                updateDto.setProvider(user.getProvider());
                updateDto.setOauthProviderId(user.getOauthProviderId());
                updateDto.setPictureId(user.getPicture() != null ? user.getPicture().getId() : null);
                
                model.addAttribute("user", updateDto);
                model.addAttribute("userId", id);
                model.addAttribute("roles", Role.values());
                model.addAttribute("providers", com.pwr_zpi.reservespotapi.entities.users.AuthProvider.values());
                return "admin/users/edit";
            })
            .orElse("redirect:/admin/users");
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute("user") UpdateUserDto updateDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("providers", com.pwr_zpi.reservespotapi.entities.users.AuthProvider.values());
            return "admin/users/edit";
        }
        
        try {
            userService.updateUser(id, updateDto)
                .ifPresentOrElse(
                    userDto -> redirectAttributes.addFlashAttribute("success", "User updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "User not found")
                );
            return "redirect:/admin/users/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("providers", com.pwr_zpi.reservespotapi.entities.users.AuthProvider.values());
            return "admin/users/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found");
        }
        return "redirect:/admin/users";
    }
}
