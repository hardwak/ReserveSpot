package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.tag.dto.CreateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.TagDto;
import com.pwr_zpi.reservespotapi.entities.tag.dto.UpdateTagDto;
import com.pwr_zpi.reservespotapi.entities.tag.mapper.TagMapper;
import com.pwr_zpi.reservespotapi.entities.tag.service.TagService;
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
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagService tagService;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public String listTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        List<Tag> allTags = tagRepository.findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allTags = allTags.stream()
                .filter(t -> t.getName() != null && t.getName().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }
        
        allTags.sort((t1, t2) -> {
            int result = 0;
            switch (sortBy) {
                case "name" -> result = (t1.getName() != null ? t1.getName() : "").compareTo(t2.getName() != null ? t2.getName() : "");
                default -> result = t1.getId().compareTo(t2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allTags.size());
        // Ensure start is within bounds
        start = Math.min(start, allTags.size());
        List<Tag> pageTags = (start < end) ? allTags.subList(start, end) : new ArrayList<>();
        
        // Map to DTOs - mappers will access lazy collections within the transaction
        // Collections are accessed directly in mappers, which will trigger lazy loading safely
        List<TagDto> tagDtos = pageTags.stream()
            .map(tagMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("tags", tagDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allTags.size() / size));
        model.addAttribute("totalItems", allTags.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        return "admin/tags/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("tag", new CreateTagDto());
        return "admin/tags/create";
    }

    @PostMapping("/create")
    public String createTag(@Valid @ModelAttribute("tag") CreateTagDto createDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/tags/create";
        }
        
        try {
            tagService.createTag(createDto);
            redirectAttributes.addFlashAttribute("success", "Tag created successfully");
            return "redirect:/admin/tags/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/tags/create";
        }
    }

    @GetMapping("/{id}")
    public String viewTag(@PathVariable Long id, Model model) {
        return tagRepository.findById(id)
            .map(tag -> {
                TagDto tagDto = tagMapper.toDto(tag);
                model.addAttribute("tag", tagDto);
                return "admin/tags/view";
            })
            .orElse("redirect:/admin/tags/list");
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model) {
        return tagRepository.findById(id)
            .map(tag -> {
                UpdateTagDto updateDto = new UpdateTagDto();
                updateDto.setName(tag.getName());
                
                model.addAttribute("tag", updateDto);
                model.addAttribute("tagId", id);
                return "admin/tags/edit";
            })
            .orElse("redirect:/admin/tags/list");
    }

    @PostMapping("/{id}/edit")
    public String updateTag(@PathVariable Long id,
                           @Valid @ModelAttribute("tag") UpdateTagDto updateDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tag", updateDto);
            model.addAttribute("tagId", id);
            return "admin/tags/edit";
        }
        
        try {
            tagService.updateTag(id, updateDto)
                .ifPresentOrElse(
                    tagDto -> redirectAttributes.addFlashAttribute("success", "Tag updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Tag not found")
                );
            return "redirect:/admin/tags/" + id;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            model.addAttribute("tag", updateDto);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tagId", id);
            return "admin/tags/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTag(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
        boolean deleted = tagService.deleteTag(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Tag deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Tag not found");
        }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            redirectAttributes.addFlashAttribute("error", "Could not delete tag: " + e.getMessage());
        }
        return "redirect:/admin/tags/list";
    }
}

