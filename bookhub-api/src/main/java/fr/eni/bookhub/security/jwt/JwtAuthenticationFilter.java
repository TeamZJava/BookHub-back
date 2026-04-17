package fr.eni.bookhub.security.jwt;

import java.io.IOException;

import fr.eni.bookhub.bll.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.AllArgsConstructor;

//Doit être active dès qu'il y a une requête
//Doit devenir un bean pour spring
@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Injection de la couche BLL pour gérer le token
    private JwtService jwtService;
    // Injection de la couche BLL pour gérer les données de la DB
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // vérifier le jeton JWT
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);// 7 correspond à Bearer
        // Vérification de l'utilisateur
        final String userEmail = jwtService.extractUserName(jwt);// Extraire du jeton JWT
        // Validation des données par rapport à la DB
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Check in DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            // Validation du jeton JWT
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Gestion du contexte de sécurité de l’utilisateur
                //Création d'un nouveau jeton avec les informations et les rôles de l'utilisateur
                UsernamePasswordAuthenticationToken authToken = new
                        UsernamePasswordAuthenticationToken(userEmail, null,
                        userDetails.getAuthorities());
                //Transmettre les détails de la demande d’origine
                authToken.setDetails(new
                        WebAuthenticationDetailsSource().buildDetails(request));
                //Mise à jour du contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}