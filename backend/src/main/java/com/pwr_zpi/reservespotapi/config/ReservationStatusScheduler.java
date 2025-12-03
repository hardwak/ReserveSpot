package com.pwr_zpi.reservespotapi.config;

import com.pwr_zpi.reservespotapi.entities.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to automatically update reservation statuses from CONFIRMED to COMPLETED
 * when the reservation time has passed.
 * 
 * Runs every 5 minutes to check and update past reservations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationStatusScheduler {

    private final ReservationService reservationService;

    /**
     * Scheduled task that runs every 5 minutes to complete past reservations.
     * You can adjust the frequency by changing the fixedRate value or using cron expression.
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes (300000 milliseconds)
    public void updateReservationStatuses() {
        try {
            int updatedCount = reservationService.completePastReservations();
            if (updatedCount > 0) {
                log.info("Updated {} reservation(s) from CONFIRMED to COMPLETED", updatedCount);
            }
        } catch (Exception e) {
            log.error("Error updating reservation statuses", e);
        }
    }
}

