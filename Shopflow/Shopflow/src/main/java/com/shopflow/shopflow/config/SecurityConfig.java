package com.shopflow.shopflow.config;

import com.shopflow.shopflow.security.JwtAuthenticationFilter;
import com.shopflow.shopflow.security.FirebaseAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final FirebaseAuthenticationFilter firebaseAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // Public GET endpoints (anyone can view products, categories, reviews)
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/reviews/**").permitAll()
                        
                        // Category management (Admin only)
                        .requestMatchers("/api/categories/**").hasRole("ADMIN")
                        
                        // Coupon management (Admin only)
                        .requestMatchers("/api/coupons/**").hasRole("ADMIN")
                        
                        // Product management (Seller/Admin)
                        .requestMatchers("/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        
                        // Dashboards
                        .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/seller").hasRole("SELLER")
                        .requestMatchers("/api/dashboard/customer").hasRole("CUSTOMER")

                        // Cart operations (Customer)
                        .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                        
                        // Orders (Customers can place, Admin/Seller can view/manage)
                        .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN", "SELLER")
                        
                        // Reviews (Customers can create/update)
                        .requestMatchers("/api/reviews/**").hasRole("CUSTOMER")

                        // User management (Self access handled by @PreAuthorize in Controller)
                        .requestMatchers("/api/users/*").authenticated()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}