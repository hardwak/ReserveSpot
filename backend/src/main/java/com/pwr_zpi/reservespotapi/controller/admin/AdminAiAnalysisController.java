package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysis;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysisRepository;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.AiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.CreateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.UpdateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.mapper.AiAnalysisMapper;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.service.AiAnalysisService;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
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
@RequestMapping("/admin/ai-analysis")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiAnalysisController {

    private final AiAnalysisService analysisService;
    private final AiAnalysisRepository analysisRepository;
    private final AiAnalysisMapper analysisMapper;
    private final RestaurantRepository restaurantRepository;

    @GetMapping
    public String listAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long restaurantId,
            Model model) {
        
        List<AiAnalysis> allAnalyses = analysisRepository.findAll();
        
        if (restaurantId != null) {
            allAnalyses = allAnalyses.stream()
                .filter(a -> a.getRestaurant() != null && a.getRestaurant().getId().equals(restaurantId))
                .collect(Collectors.toList());
        }
        
        allAnalyses.sort((a1, a2) -> {
            int result = 0;
            switch (sortBy) {
                case "lastUpdated" -> result = (a1.getLastUpdated() != null ? a1.getLastUpdated() : java.time.LocalDateTime.MIN)
                    .compareTo(a2.getLastUpdated() != null ? a2.getLastUpdated() : java.time.LocalDateTime.MIN);
                case "sentimentScore" -> result = Double.compare(a1.getSentimentScore() != null ? a1.getSentimentScore() : 0.0, 
                                                                 a2.getSentimentScore() != null ? a2.getSentimentScore() : 0.0);
                default -> result = a1.getId().compareTo(a2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allAnalyses.size());
        List<AiAnalysis> pageAnalyses = allAnalyses.subList(Math.min(start, allAnalyses.size()), end);
        
        List<AiAnalysisDto> analysisDtos = pageAnalyses.stream()
            .map(analysisMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("analyses", analysisDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allAnalyses.size() / size));
        model.addAttribute("totalItems", allAnalyses.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("restaurantId", restaurantId);
        
        return "admin/ai-analysis/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("analysis", new CreateAiAnalysisDto());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        return "admin/ai-analysis/create";
    }

    @PostMapping("/create")
    public String createAnalysis(@Valid @ModelAttribute("analysis") CreateAiAnalysisDto createDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/ai-analysis/create";
        }
        
        try {
            analysisService.createAnalysis(createDto);
            redirectAttributes.addFlashAttribute("success", "AI Analysis created successfully");
            return "redirect:/admin/ai-analysis";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/ai-analysis/create";
        }
    }

    @GetMapping("/{id}")
    public String viewAnalysis(@PathVariable Long id, Model model) {
        return analysisRepository.findById(id)
            .map(analysis -> {
                AiAnalysisDto analysisDto = analysisMapper.toDto(analysis);
                model.addAttribute("analysis", analysisDto);
                return "admin/ai-analysis/view";
            })
            .orElse("redirect:/admin/ai-analysis");
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return analysisRepository.findById(id)
            .map(analysis -> {
                UpdateAiAnalysisDto updateDto = new UpdateAiAnalysisDto();
                updateDto.setSummaryText(analysis.getSummaryText());
                updateDto.setSentimentScore(analysis.getSentimentScore());
                
                model.addAttribute("analysis", updateDto);
                model.addAttribute("analysisId", id);
                model.addAttribute("restaurants", restaurantRepository.findAll());
                return "admin/ai-analysis/edit";
            })
            .orElse("redirect:/admin/ai-analysis");
    }

    @PostMapping("/{id}/edit")
    public String updateAnalysis(@PathVariable Long id,
                                @Valid @ModelAttribute("analysis") UpdateAiAnalysisDto updateDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("analysisId", id);
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/ai-analysis/edit";
        }
        
        try {
            analysisService.updateAnalysis(id, updateDto)
                .ifPresentOrElse(
                    analysisDto -> redirectAttributes.addFlashAttribute("success", "AI Analysis updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "AI Analysis not found")
                );
            return "redirect:/admin/ai-analysis/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("analysisId", id);
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/ai-analysis/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAnalysis(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = analysisService.deleteAnalysis(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "AI Analysis deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "AI Analysis not found");
        }
        return "redirect:/admin/ai-analysis";
    }
}

