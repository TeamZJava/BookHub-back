package fr.eni.bookhub.security;

import jakarta.servlet.Filter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class BookHubSecurityConfig {
    protected final Log logger = LogFactory.getLog(getClass());
    // Contexte de JWT :
    @Autowired
    private Filter jwtAuthenticationFilter;
    @Autowired
    private AuthenticationProvider authenticationProvider;

    //Le front et l’API ne sont pas sur le même port ; le navigateur bloque les appels entre
    // origines différentes ; on configure le back pour autoriser l’origine du front (CORS),
    // sinon la connexion depuis Angular ne peut pas fonctionner.
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Restriction des URLs selon la connexion utilisateur et leurs rôles
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.authorizeHttpRequests(auth-> {
            auth
                    //Permettre l'accès à l'URL login et register à tout le monde
                    // + a la doc swagger
                    .requestMatchers(
                            "/api/auth/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/v3/api-docs"
                    ).permitAll()

                    // Users
                    .requestMatchers("/api/users","/api/users/**", "/api/users/**").hasAnyRole("USER","LIBRARIAN" ,"ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/users/{id}/role", "/api/users/{id}/active").hasAnyRole("ADMIN")

                    // Livres : lecture pour tout utilisateur authentifié
                    .requestMatchers(HttpMethod.GET, "/api/books/**").authenticated()

                    // Livres : création et modification et suppression réservées aux LIBRARIAN et ADMIN
                    .requestMatchers(HttpMethod.POST, "/api/books").hasAnyRole("LIBRARIAN", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

                    // Commentaires et notes : tout utilisateur authentifié
                    .requestMatchers(HttpMethod.POST, "/api/books/**").authenticated()

                    // Favoris, emprunts, réservations : tout utilisateur authentifié
                    .requestMatchers("/api/favorites/**").authenticated()

                    // Emprunts
                    .requestMatchers(HttpMethod.PUT, "/api/loans/*/return").hasAnyRole("LIBRARIAN", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/loans").hasAnyRole("LIBRARIAN", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/loans").hasAnyRole("LIBRARIAN", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/loans/my", "/api/loans/is-late").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/loans").authenticated()

                    // Reservations
                    .requestMatchers("/api/reservations", "/api/reservations/*").authenticated()

                    // Toute autre URL est refusée
                    .anyRequest().denyAll();
        });


        // Désactiver Cross Site Request Forgery
        // Inutile pour les API REST en Stateless
        http.csrf(csrf -> {
            csrf.disable();
        });
        //Connexion de l'utilisateur
        http.authenticationProvider(authenticationProvider);
        //Activer le filtre JWT et l'authentication de l'utilisateur
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // Session Stateless
        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });
        return http.build();
    }
}
