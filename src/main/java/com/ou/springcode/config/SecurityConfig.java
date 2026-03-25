package com.ou.springcode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ou.springcode.security.CustomAccessDeniedHandler;
import com.ou.springcode.security.JwtAuthenticationEntryPoint;
import com.ou.springcode.security.JwtAuthenticationFilter;
import com.ou.springcode.security.JwtService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        CustomAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                                            .requestMatchers("/public/**").permitAll()
                                            .requestMatchers("/api/auth/**").permitAll()
                                            .requestMatchers("/h2-console/**").permitAll()
                                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                            .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")
                                            .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                        .accessDeniedHandler(accessDeniedHandler))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
            http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

            return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
     return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }
}
