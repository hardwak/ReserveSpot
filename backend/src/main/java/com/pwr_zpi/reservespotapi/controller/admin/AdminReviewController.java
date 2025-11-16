package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.entities.review.dto.CreateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.UpdateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.mapper.ReviewMapper;
import com.pwr_zpi.reservespotapi.entities.review.service.ReviewService;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
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
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @GetMapping("/list")
    public String listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) Integer minRating,
            Model model) {
        
        List<Review> allReviews = reviewRepository.findAll();
        
        if (userId != null) {
            allReviews = allReviews.stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        }
        
        if (restaurantId != null) {
            allReviews = allReviews.stream()
                .filter(r -> r.getRestaurant() != null && r.getRestaurant().getId().equals(restaurantId))
                .collect(Collectors.toList());
        }
        
        if (minRating != null) {
            allReviews = allReviews.stream()
                .filter(r -> r.getRating() != null && r.getRating() >= minRating)
                .collect(Collectors.toList());
        }
        
        allReviews.sort((r1, r2) -> {
            int result = 0;
            switch (sortBy) {
                case "rating" -> result = Integer.compare(r1.getRating() != null ? r1.getRating() : 0, 
                                                          r2.getRating() != null ? r2.getRating() : 0);
                case "createdAt" -> result = (r1.getCreatedAt() != null ? r1.getCreatedAt() : java.time.LocalDateTime.MIN)
                    .compareTo(r2.getCreatedAt() != null ? r2.getCreatedAt() : java.time.LocalDateTime.MIN);
                default -> result = r1.getId().compareTo(r2.getId());
            }
            return sortDir.equalsIgnoreCase("desc") ? -result : result;
        });
        
        int start = page * size;
        int end = Math.min(start + size, allReviews.size());
        List<Review> pageReviews = allReviews.subList(Math.min(start, allReviews.size()), end);
        
        List<ReviewDto> reviewDtos = pageReviews.stream()
            .map(reviewMapper::toDto)
            .collect(Collectors.toList());
        
        model.addAttribute("reviews", reviewDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (int) Math.ceil((double) allReviews.size() / size));
        model.addAttribute("totalItems", allReviews.size());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("userId", userId);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("minRating", minRating);
        
        return "admin/reviews/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("review", new CreateReviewDto());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("restaurants", restaurantRepository.findAll());
        return "admin/reviews/create";
    }

    @PostMapping("/create")
    public String createReview(@RequestParam Long userId,
                              @Valid @ModelAttribute("review") CreateReviewDto createDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/reviews/create";
        }
        
        try {
            reviewService.createReview(createDto, userId);
            redirectAttributes.addFlashAttribute("success", "Review created successfully");
            return "redirect:/admin/reviews/list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("restaurants", restaurantRepository.findAll());
            return "admin/reviews/create";
        }
    }

    @GetMapping("/{id}")
    public String viewReview(@PathVariable Long id, Model model) {
        return reviewRepository.findById(id)
            .map(review -> {
                ReviewDto reviewDto = reviewMapper.toDto(review);
                model.addAttribute("review", reviewDto);
                return "admin/reviews/view";
            })
            .orElse("redirect:/admin/reviews/list");
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return reviewRepository.findById(id)
            .map(review -> {
                UpdateReviewDto updateDto = new UpdateReviewDto();
                updateDto.setPhoneNumber(review.getPhoneNumber());
                updateDto.setRating(review.getRating());
                updateDto.setComment(review.getComment());
                updateDto.setPic(review.getPic());
                
                model.addAttribute("review", updateDto);
                model.addAttribute("reviewId", id);
                return "admin/reviews/edit";
            })
            .orElse("redirect:/admin/reviews/list");
    }

    @PostMapping("/{id}/edit")
    public String updateReview(@PathVariable Long id,
                              @Valid @ModelAttribute("review") UpdateReviewDto updateDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("reviewId", id);
            return "admin/reviews/edit";
        }
        
        try {
            reviewService.updateReview(id, updateDto)
                .ifPresentOrElse(
                    reviewDto -> redirectAttributes.addFlashAttribute("success", "Review updated successfully"),
                    () -> redirectAttributes.addFlashAttribute("error", "Review not found")
                );
            return "redirect:/admin/reviews/list/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reviewId", id);
            return "admin/reviews/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id, null); // Admin can delete any review
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Review not found or could not be deleted");
        }
        return "redirect:/admin/reviews/list";
    }
}

