package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.picture.PictureRepository;
import com.pwr_zpi.reservespotapi.entities.picture.dto.CreatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.PictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.UpdatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.mapper.PictureMapper;
import com.pwr_zpi.reservespotapi.entities.picture.service.PictureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/pictures")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPictureController {

    private final PictureService pictureService;
    private final PictureRepository pictureRepository;
    private final PictureMapper pictureMapper;

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public String listPictures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        List<Picture> allPictures = pictureRepository.findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allPictures = allPictures.stream()
                .filter(p -> (p.getUrl() != null && p.getUrl().toLowerCase().contains(searchLower)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }
        
        allPictures.sort((p1, p2) -> {
            int result = 0;
            switch (sortBy) {
                case "url" -> result = (p1.getUrl() != null ? p1.getUrl() : "").compareTo(p2.getUrl() != null ? p2.getUrl() : "");
                case "uploadedAt" -> result = (p1.getUploadedAt() != null ? p1.getUploadedAt() : java.time.LocalDateTime.MIN)
                    .compareTo(p2.getUploadedAt() != null ? p2.getUploadedAt() : java.time.LocalDateTime.MIN);
                default -> result = p1.getId().compareTo(p2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allPictures.size());
        // Ensure start is within bounds
        start = Math.min(start, allPictures.size());
        List<Picture> pagePictures = (start < end) ? allPictures.subList(start, end) : new ArrayList<>();
        
        // Map to DTOs - mappers will access lazy collections within the transaction
        // Collections are accessed directly in mappers, which will trigger lazy loading safely
        List<PictureDto> pictureDtos = pagePictures.stream()
            .map(pictureMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("pictures", pictureDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allPictures.size() / size));
        model.addAttribute("totalItems", allPictures.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        return "admin/pictures/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("picture", new CreatePictureDto());
        return "admin/pictures/create";
    }

    @PostMapping("/create")
    public String createPicture(@Valid @ModelAttribute("picture") CreatePictureDto createDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/pictures/create";
        }
        
        try {
            pictureService.createPicture(createDto);
            redirectAttributes.addFlashAttribute("success", "Picture created successfully");
            return "redirect:/admin/pictures/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/pictures/create";
        }
    }

    @GetMapping("/{id}")
    public String viewPicture(@PathVariable Long id, Model model) {
        return pictureRepository.findById(id)
            .map(picture -> {
                PictureDto pictureDto = pictureMapper.toDto(picture);
                model.addAttribute("picture", pictureDto);
                return "admin/pictures/view";
            })
            .orElse("redirect:/admin/pictures/list");
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model) {
        return pictureRepository.findById(id)
            .map(picture -> {
                UpdatePictureDto updateDto = new UpdatePictureDto();
                updateDto.setUrl(picture.getUrl());
                updateDto.setDescription(picture.getDescription());
                
                model.addAttribute("picture", updateDto);
                model.addAttribute("pictureId", id);
                return "admin/pictures/edit";
            })
            .orElse("redirect:/admin/pictures/list");
    }

    @PostMapping("/{id}/edit")
    public String updatePicture(@PathVariable Long id,
                               @Valid @ModelAttribute("picture") UpdatePictureDto updateDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("picture", updateDto);
            model.addAttribute("pictureId", id);
            return "admin/pictures/edit";
        }
        
        try {
            pictureService.updatePicture(id, updateDto)
                .ifPresentOrElse(
                    pictureDto -> redirectAttributes.addFlashAttribute("success", "Picture updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Picture not found")
                );
            return "redirect:/admin/pictures/" + id;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            model.addAttribute("picture", updateDto);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pictureId", id);
            return "admin/pictures/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deletePicture(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
        boolean deleted = pictureService.deletePicture(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Picture deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Picture not found");
        }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            redirectAttributes.addFlashAttribute("error", "Could not delete picture: " + e.getMessage());
        }
        return "redirect:/admin/pictures/list";
    }
}

