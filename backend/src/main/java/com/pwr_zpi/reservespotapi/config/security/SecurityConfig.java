package com.pwr_zpi.reservespotapi.config.security;

import com.pwr_zpi.reservespotapi.config.security.oauth2login.CustomOAuth2UserService;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    // Public endpoints
                    auth.requestMatchers("/api/auth/**").permitAll();
//                    auth.requestMatchers("/api/service-points").permitAll();
//                    auth.requestMatchers("/api/service-points/{id}").permitAll();
//                    auth.requestMatchers("/api/service-points/city/{city}").permitAll();
//                    auth.requestMatchers("/api/service-points/active").permitAll();

                    // Protected endpoints
//                    auth.requestMatchers("/api/service-points/firm/**").hasAnyRole("FIRM", "ADMIN");
//                    auth.requestMatchers("/api/service-points").hasAnyRole("FIRM", "ADMIN");
//                    auth.requestMatchers("/api/service-points/{id}").hasAnyRole("FIRM", "ADMIN");

                    // Other endpoints
                    auth.requestMatchers("/", "/login").permitAll();
                    auth.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .defaultSuccessUrl("http://localhost:8080/swagger-ui/index.html", true)) //TODO change endpoint for production
                .formLogin(form -> form
                        .defaultSuccessUrl("http://localhost:8080/swagger-ui/index.html", true)
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/login?expired"))
                .build();
    }

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
