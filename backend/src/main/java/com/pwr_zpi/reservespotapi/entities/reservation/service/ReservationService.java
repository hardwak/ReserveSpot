package com.pwr_zpi.reservespotapi.entities.reservation.service;

import com.pwr_zpi.reservespotapi.entities.reservation.dto.CreateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.ReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.dto.UpdateReservationDto;
import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public Optional<ReservationDto> getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDto);
    }

    public List<ReservationDto> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getReservationsByTableId(Long tableId) {
        return reservationRepository.findByTableId(tableId)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getReservationsByStatus(String status) {
        return reservationRepository.findByStatus(status)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByReservationDatetimeBetween(startDate, endDate)
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public ReservationDto createReservation(CreateReservationDto createDto) {
        Reservation reservation = reservationMapper.toEntity(createDto);
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(savedReservation);
    }

    public Optional<ReservationDto> updateReservation(Long id, UpdateReservationDto updateDto) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservationMapper.updateEntity(updateDto, reservation);
                    Reservation savedReservation = reservationRepository.save(reservation);
                    return reservationMapper.toDto(savedReservation);
                });
    }

    public boolean deleteReservation(Long id) {
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    public long count() {
        return reservationRepository.count();
    }
}
