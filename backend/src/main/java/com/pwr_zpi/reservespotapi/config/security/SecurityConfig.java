package com.pwr_zpi.reservespotapi.config.security;

import com.pwr_zpi.reservespotapi.config.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    // Public endpoints (Klient niezalogowany)
                    auth.requestMatchers("/api/auth/**").permitAll(); // Login, Register, Reset password
                    auth.requestMatchers("/api/restaurants").permitAll(); // Wyszukiwanie restauracji
                    auth.requestMatchers("/api/restaurants/search/**").permitAll();
                    auth.requestMatchers("/api/restaurants/{id}").permitAll(); // Get restaurant by ID
                    auth.requestMatchers("/api/restaurants/city/**").permitAll(); // Search by city
                    auth.requestMatchers("/api/reviews").permitAll(); // Przeglądanie opinii
                    auth.requestMatchers("/api/reviews/{id}").permitAll(); // Get review by ID
                    auth.requestMatchers("/api/reviews/restaurant/**").permitAll(); // Get reviews by restaurant
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    //TODO add html pages to resolve ERR_TOO_MANY_REDIRECTS
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**")))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/login", "/css/**", "/js/**").permitAll();
                    auth.requestMatchers("/swagger-ui/**").permitAll();
                    auth.requestMatchers("/v3/api-docs/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("http://localhost:8080/swagger-ui/index.html", true)
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .build();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .authorizeHttpRequests(auth -> {
//                    // Public endpoints
//                    auth.requestMatchers("/api/auth/**").permitAll();
////                    auth.requestMatchers("/api/service-points").permitAll();
////                    auth.requestMatchers("/api/service-points/{id}").permitAll();
////                    auth.requestMatchers("/api/service-points/city/{city}").permitAll();
////                    auth.requestMatchers("/api/service-points/active").permitAll();
//
//                    // Protected endpoints
////                    auth.requestMatchers("/api/service-points/firm/**").hasAnyRole("FIRM", "ADMIN");
////                    auth.requestMatchers("/api/service-points").hasAnyRole("FIRM", "ADMIN");
////                    auth.requestMatchers("/api/service-points/{id}").hasAnyRole("FIRM", "ADMIN");
//
//                    // Other endpoints
//                    auth.requestMatchers("/", "/login").permitAll();
//                    auth.anyRequest().authenticated();
//                })
//                .oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService))
//                        .defaultSuccessUrl("http://localhost:8080/swagger-ui/index.html", true))
//                .formLogin(form -> form
//                        .defaultSuccessUrl("http://localhost:8080/swagger-ui/index.html", true)
//                        .permitAll())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                        .maximumSessions(1)
//                        .expiredUrl("/login?expired"))
//                .build();
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));//TODO modify app endpoint
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
