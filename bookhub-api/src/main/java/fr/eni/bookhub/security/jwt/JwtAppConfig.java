package fr.eni.bookhub.security.jwt;

import fr.eni.bookhub.dal.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class JwtAppConfig {
    private final UserRepository uRepository;
    public JwtAppConfig(UserRepository uRepository) {
        this.uRepository = uRepository;
    }
    /**
     * Fournit le service qui charge les utilisateurs depuis la base de données
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> uRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    /**
     * Fournisseur d'authentification personnalisé
     */
    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder
            passwordEncoder) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new BadCredentialsException("Bad credentials");
                }
                return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
            }
            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }
    /**
     * Gestionnaire d'authentification basé sur le fournisseur personnalisé
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }
    /**
     * Encodeur de mot de passe
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}