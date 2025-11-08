package com.pwr_zpi.reservespotapi.entities.picture;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "pictures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Picture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    private LocalDateTime uploadedAt;

    @Column(columnDefinition = "text")
    private String description;

    @OneToOne(mappedBy = "picture")
    private User user;

    @ManyToMany(mappedBy = "pictures")
    private Set<Restaurant> restaurants;

    @ManyToMany(mappedBy = "pictures")
    private Set<Review> reviews;
}