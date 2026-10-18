package br.com.capitulando.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.capitulando.repository.UserRepository;

@Configuration
public class SecurityConfiguration {
        @Bean
        UserDetailsService userDetailsService(UserRepository userRepository) {
                return email -> userRepository.findByEmailIgnoreCase(email)
                                .map(user -> org.springframework.security.core.userdetails.User
                                                .withUsername(user.getEmail())
                                                .password(user.getPasswordHash())
                                                .disabled(!user.isEmailVerified())
                                                .authorities("ROLE_USER")
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/auth/register",
                                                                "/auth/verify-email",
                                                                "/auth/resend-verification",
                                                                "/auth/login",
                                                                "/auth/forgot-password",
                                                                "/auth/reset-password",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/auth/me")
                                                .authenticated()
                                                .anyRequest()
                                                .authenticated())
                                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
                return http.build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
                List<String> origins = Arrays.stream(frontendUrl.split(","))
                                .map(origin -> origin.trim())
                                .filter(s -> !s.isEmpty())
                                .map(s -> s.replaceAll("/+$", ""))
                                .toList();
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(origins);
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
                configuration.setAllowCredentials(false);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
