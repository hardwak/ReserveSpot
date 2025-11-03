package com.pwr_zpi.reservespotapi.entities.users;

import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Column(columnDefinition = "text")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //sso
    private String oauthProviderId;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    //

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Picture picture;

    @OneToMany(mappedBy = "owner")
    private Set<Restaurant> restaurants;

    @OneToMany(mappedBy = "user")
    private Set<Reservation> reservations;

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (role) {
            case ADMIN -> List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_RESTAURANT"),
                    new SimpleGrantedAuthority("ROLE_CLIENT")
            );
            case CLIENT -> List.of(
                    new SimpleGrantedAuthority("ROLE_CLIENT")
            );
            case RESTAURANT -> List.of(
                    new SimpleGrantedAuthority("ROLE_RESTAURANT")
            );
        };
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
