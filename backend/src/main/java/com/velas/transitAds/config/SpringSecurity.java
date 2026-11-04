package com.velas.transitAds.config;

import com.velas.transitAds.security.authFilter.AuthFilter;
import com.velas.transitAds.security.authFilter.EmailVerificationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SpringSecurity {

    @Bean
    public SecurityFilterChain securityFilterChain(
            AuthFilter authFilter,
            EmailVerificationFilter emailVerificationFilter,
            HttpSecurity http,
            UrlBasedCorsConfigurationSource corsConfigurationSource
    ) throws Exception{
        return http.authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/auth/register/driver",
                                "/auth/register/company",
                                "/auth/login/driver",
                                "/auth/login/company",
                                "/auth/refresh",
                                "/health-check"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(emailVerificationFilter, AuthFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
