package com.pwr_zpi.reservespotapi.controller.admin;

import com.pwr_zpi.reservespotapi.entities.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userService.count());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersPage() {
        return "redirect:/admin/users/list";
    }

    @GetMapping("/restaurants")
    public String restaurantsPage() {
        return "redirect:/admin/restaurants/list";
    }

    @GetMapping("/reservations")
    public String reservationsPage() {
        return "redirect:/admin/reservations/list";
    }

    @GetMapping("/reviews")
    public String reviewsPage() {
        return "redirect:/admin/reviews/list";
    }

    @GetMapping("/tags")
    public String tagsPage() {
        return "redirect:/admin/tags/list";
    }

    @GetMapping("/tables")
    public String tablesPage() {
        return "redirect:/admin/tables/list";
    }

    @GetMapping("/statistics")
    public String statisticsPage() {
        return "redirect:/admin/statistics/list";
    }

    @GetMapping("/pictures")
    public String picturesPage() {
        return "redirect:/admin/pictures/list";
    }

    @GetMapping("/ai-analysis")
    public String aiAnalysisPage() {
        return "redirect:/admin/ai-analysis/list";
    }
}

