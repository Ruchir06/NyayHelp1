package com.nyayhelp.caseservice.config;

import com.nyayhelp.caseservice.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cases/create").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cases/*/apply").authenticated()
                        .requestMatchers(HttpMethod.PUT,  "/api/cases/*/select").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cases/*/end-chat").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/cases/client").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/cases/lawyer/accepted").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/cases/lawyer").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/cases/*/applications").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/cases/{id}").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}