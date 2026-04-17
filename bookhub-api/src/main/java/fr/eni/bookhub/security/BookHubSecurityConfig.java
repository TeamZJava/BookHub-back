package fr.eni.bookhub.security;

import jakarta.servlet.Filter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class BookHubSecurityConfig {
    protected final Log logger = LogFactory.getLog(getClass());
    // Contexte de JWT :
    @Autowired
    private Filter jwtAuthenticationFilter;
    @Autowired
    private AuthenticationProvider authenticationProvider;



    // Restriction des URLs selon la connexion utilisateur et leurs rôles
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                    .requestMatchers("/api/users","/api/users/**", "/api/users/**").hasAnyRole("USER","LIBRARIAN" ,"ADMIN")

                    //Toutes autres url et méthodes HTTP ne sont pas permises
                    .anyRequest().denyAll();
        });

        // désactivation CSRF pour faire des requêtes postman
        http.csrf(AbstractHttpConfigurer::disable);

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
