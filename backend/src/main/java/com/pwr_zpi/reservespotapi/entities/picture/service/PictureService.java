package com.pwr_zpi.reservespotapi.entities.picture.service;

import com.pwr_zpi.reservespotapi.entities.picture.dto.CreatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.PictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.UpdatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.picture.PictureRepository;
import com.pwr_zpi.reservespotapi.entities.picture.mapper.PictureMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PictureService {

    private final PictureRepository pictureRepository;
    private final PictureMapper pictureMapper;

    public List<PictureDto> getAllPictures() {
        return pictureRepository.findAll()
                .stream()
                .map(pictureMapper::toDto)
                .toList();
    }

    public Optional<PictureDto> getPictureById(Long id) {
        return pictureRepository.findById(id)
                .map(pictureMapper::toDto);
    }

    public List<PictureDto> getPicturesByUrl(String url) {
        return pictureRepository.findByUrlContainingIgnoreCase(url)
                .stream()
                .map(pictureMapper::toDto)
                .toList();
    }

    public List<PictureDto> getPicturesByDescription(String description) {
        return pictureRepository.findByDescriptionContainingIgnoreCase(description)
                .stream()
                .map(pictureMapper::toDto)
                .toList();
    }

    public PictureDto createPicture(CreatePictureDto createDto) {
        Picture picture = pictureMapper.toEntity(createDto);
        Picture savedPicture = pictureRepository.save(picture);
        return pictureMapper.toDto(savedPicture);
    }

    public Optional<PictureDto> updatePicture(Long id, UpdatePictureDto updateDto) {
        return pictureRepository.findById(id)
                .map(picture -> {
                    pictureMapper.updateEntity(updateDto, picture);
                    Picture savedPicture = pictureRepository.save(picture);
                    return pictureMapper.toDto(savedPicture);
                });
    }

    public boolean deletePicture(Long id) {
        if (pictureRepository.existsById(id)) {
            pictureRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return pictureRepository.existsById(id);
    }

    public long count() {
        return pictureRepository.count();
    }
}
