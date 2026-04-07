package com.pos.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()  // Permitir TODAS las rutas
                )
                .formLogin(form -> form.disable())  // Deshabilitar login form
                .httpBasic(basic -> basic.disable());  // Deshabilitar basic auth

        return http.build();
    }
}