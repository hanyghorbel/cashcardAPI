package com.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(request -> request
                    .requestMatchers("/cashcard/**")
                    .hasRole("CARD-OWNER"))
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable); // IMPORTANT: check to how to disable csrf
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService testOnlyUser(PasswordEncoder passwordEncoder) {
        User.UserBuilder user = User.builder();
        UserDetails sarah = user
                .username("sarah")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER") // No roles for now
                .build();
        UserDetails keith = user
                .username("keith")
                .password(passwordEncoder.encode("magtop"))
                .roles("NON-OWNER") // No roles for now
                .build();
        UserDetails vishal = user
                .username("vishal")
                .password(passwordEncoder.encode("123"))
                .roles("CARD-OWNER") // No roles for now
                .build();
        return new InMemoryUserDetailsManager(sarah, keith, vishal);
    }
}
