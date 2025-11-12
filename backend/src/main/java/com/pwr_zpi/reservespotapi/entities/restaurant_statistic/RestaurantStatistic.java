package com.pwr_zpi.reservespotapi.entities.restaurant_statistic;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Entity
@Table(name = "statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RestaurantStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    private Integer hourOfDay;

    private Double averageOccupancy;

    private LocalDate date;
}
