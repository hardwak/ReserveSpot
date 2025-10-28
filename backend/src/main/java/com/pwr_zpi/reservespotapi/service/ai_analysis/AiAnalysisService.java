package com.pwr_zpi.reservespotapi.service.ai_analysis;

import com.pwr_zpi.reservespotapi.dto.ai_analysis.AiAnalysisDto;
import com.pwr_zpi.reservespotapi.dto.ai_analysis.CreateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.dto.ai_analysis.UpdateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysis;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysisRepository;
import com.pwr_zpi.reservespotapi.mapper.ai_analysis.AiAnalysisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiAnalysisService {

    private final AiAnalysisRepository analysisRepository;
    private final AiAnalysisMapper analysisMapper;

    public List<AiAnalysisDto> getAllAnalyses() {
        return analysisRepository.findAll()
                .stream()
                .map(analysisMapper::toDto)
                .toList();
    }

    public Optional<AiAnalysisDto> getAnalysisById(Long id) {
        return analysisRepository.findById(id)
                .map(analysisMapper::toDto);
    }

    public Optional<AiAnalysisDto> getAnalysisByRestaurantId(Long restaurantId) {
        return analysisRepository.findByRestaurantId(restaurantId)
                .map(analysisMapper::toDto);
    }

    public AiAnalysisDto createAnalysis(CreateAiAnalysisDto createDto) {
        AiAnalysis analysis = analysisMapper.toEntity(createDto);
        AiAnalysis savedAnalysis = analysisRepository.save(analysis);
        return analysisMapper.toDto(savedAnalysis);
    }

    public Optional<AiAnalysisDto> updateAnalysis(Long id, UpdateAiAnalysisDto updateDto) {
        return analysisRepository.findById(id)
                .map(analysis -> {
                    analysisMapper.updateEntity(updateDto, analysis);
                    AiAnalysis savedAnalysis = analysisRepository.save(analysis);
                    return analysisMapper.toDto(savedAnalysis);
                });
    }

    public boolean deleteAnalysis(Long id) {
        if (analysisRepository.existsById(id)) {
            analysisRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return analysisRepository.existsById(id);
    }

    public long count() {
        return analysisRepository.count();
    }
}
