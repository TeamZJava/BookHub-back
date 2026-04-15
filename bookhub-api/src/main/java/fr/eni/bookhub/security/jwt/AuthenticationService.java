package fr.eni.bookhub.security.jwt;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthenticationService {
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setToken(jwtToken);
        return authResponse;
    }
}