package Therapie_Management.backend.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @EnableMethodSecurity aktiviert @PreAuthorize an den Controllern (PatientController,
 * TherapeutController) - dort wird geprueft, ob die Pfad-ID zur eingeloggten Person passt.
 * Diese Klasse selbst regelt nur die grobe Ebene: welche Endpoints ueberhaupt ein gueltiges
 * Token brauchen.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) throws Exception {
        http
            // Stateless REST-API mit JWT statt Session-Cookies -> kein CSRF-Schutz noetig.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> {
                requests.requestMatchers("/api/auth/login", "/api/auth/register").permitAll();
                // Nur freigegeben, wenn die Console explizit aktiviert wurde (lokale
                // Entwicklung) - sie ist ein DB-Admin-Tool ohne Anwendungs-Token-Schutz.
                if (h2ConsoleEnabled) {
                    requests.requestMatchers("/h2-console/**").permitAll();
                }
                requests.anyRequest().authenticated();
            })
            // Die H2-Console rendert sich selbst in einem <iframe>; Spring Securitys Standard
            // "X-Frame-Options: DENY" wuerde das Anzeigen im Browser sonst blockieren.
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            // Fehlt/greift kein Token, scheitert die Anfrage schon im Filter, bevor ueberhaupt
            // ein Controller erreicht wird - der GlobalExceptionHandler (@RestControllerAdvice)
            // kann das also nicht abfangen. Ohne diesen Block wuerde Spring Security stattdessen
            // auf eine HTML-Login-Seite umleiten. @PreAuthorize-Verstoesse (falsche Rolle/ID)
            // passieren dagegen erst beim Controller-Aufruf und landen deshalb regulaer im
            // GlobalExceptionHandler (AccessDeniedException).
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Kein gueltiges Token"))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Erlaubt den konfigurierten Frontend-Origins (app.cors.allowed-origins), die API aufzurufen. */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
