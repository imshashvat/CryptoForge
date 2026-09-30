package com.nietproject.cryptoforge.config;

import com.nietproject.cryptoforge.security.JwtAuthenticationFilter;
import com.nietproject.cryptoforge.service.UserDetailsServiceImpl;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl    userDetailsService;
    private final JwtAuthenticationFilter   jwtAuthFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter      = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 10 is Spring Security's recommended default — cryptographically solid
        // and ~4x faster than 12, which was adding ~500ms to every login/register call.
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable Spring Security's built-in form login & basic auth —
            // our WebViewController handles login/logout directly
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow internal JSP forwards
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE).permitAll()
                // Completely public — landing page, static assets, auth APIs, JSP views
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/favicon.ico",
                    "/assets/**",
                    "/WEB-INF/views/**",
                    "/api/auth/**",
                    "/api/assets",
                    "/api/assets/**",
                    "/api/prices",
                    "/api/leaderboard",
                    "/api/market/**",
                    "/login",
                    "/register",
                    "/loginError",
                    "/logout",
                    "/dashboard",
                    "/portfolio",
                    "/order",
                    "/transactions",
                    "/leaderboard",
                    "/alerts",
                    "/alerts/**",
                    "/audit/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/error"
                ).permitAll()
                // Admin-only routes
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            // IF_REQUIRED: sessions created only when needed (JSP views use HttpSession)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://*.vercel.app",
            "https://cryptoforge.vercel.app",
            "https://*.onrender.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
