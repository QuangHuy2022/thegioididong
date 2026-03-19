package com.example.demo_3001.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**", "/register", "/login", "/403", "/product/**", "/category/**").permitAll()
                .requestMatchers("/vouchers/redeem/**", "/vouchers/verify-otp/**").hasRole("USER")
                .requestMatchers("/vouchers/**").hasRole("ADMIN")
                .requestMatchers("/orders/checkout", "/orders/place", "/orders/my-orders", "/orders/cancel/**", "/orders/momo-callback", "/orders/success").authenticated()
                .requestMatchers("/orders/**").hasRole("ADMIN")
                .requestMatchers("/admin/**", "/categories/**", "/users/**").hasRole("ADMIN")
                .requestMatchers("/products/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/403")
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity in this demo, though not recommended for production

        return http.build();
    }
}
