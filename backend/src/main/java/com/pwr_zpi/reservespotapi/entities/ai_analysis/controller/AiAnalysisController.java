package com.pwr_zpi.reservespotapi.entities.ai_analysis.controller;

import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.AiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.CreateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.UpdateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai-analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiAnalysisController {

    private final AiAnalysisService analysisService;

    @GetMapping
    public ResponseEntity<List<AiAnalysisDto>> getAllAnalyses() {
        List<AiAnalysisDto> analyses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiAnalysisDto> getAnalysisById(@PathVariable Long id) {
        Optional<AiAnalysisDto> analysis = analysisService.getAnalysisById(id);
        return analysis.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<AiAnalysisDto> getAnalysisByRestaurant(@PathVariable Long restaurantId) {
        Optional<AiAnalysisDto> analysis = analysisService.getAnalysisByRestaurantId(restaurantId);
        return analysis.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AiAnalysisDto> createAnalysis(@Valid @RequestBody CreateAiAnalysisDto createDto) {
        AiAnalysisDto createdAnalysis = analysisService.createAnalysis(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnalysis);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AiAnalysisDto> updateAnalysis(@PathVariable Long id, 
                                                      @Valid @RequestBody UpdateAiAnalysisDto updateDto) {
        Optional<AiAnalysisDto> updatedAnalysis = analysisService.updateAnalysis(id, updateDto);
        return updatedAnalysis.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable Long id) {
        boolean deleted = analysisService.deleteAnalysis(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getAnalysisCount() {
        long count = analysisService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> analysisExists(@PathVariable Long id) {
        boolean exists = analysisService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
