package fr.eni.bookhub.bll;

import fr.eni.bookhub.dto.authentification.RegisterRequest;
import fr.eni.bookhub.security.jwt.AuthenticationRequest;
import fr.eni.bookhub.security.jwt.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    // Création d'un utilisateur avant chaque test pour avoir quelqu'un à connecter
    @BeforeEach
    void setup() {
        RegisterRequest userToConnect = RegisterRequest.builder()
                .email("user@bookhub.fr")
                .password("Password123!")
                .firstName("UserFirstName")
                .lastName("UserLastName")
                .tosAcceptationDate(LocalDateTime.now())
                .build();

        userService.add(userToConnect);
    }

    @Test
    void connection_passed() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@bookhub.fr")
                .password("Password123!")
                .build();

        AuthenticationResponse response = authenticationService.authenticate(request);

        // Réponse du login non nulle
        assertNotNull(response);
        // Token JWT présent
        assertNotNull(response.getToken());
    }

    @Test
    void connection_wrong_password() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@bookhub.fr")
                .password("Password")
                .build();

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void connection_wrong_email() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("notauser@bookhub.fr")
                .password("Password123!")
                .build();

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void token_is_valid() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@bookhub.fr")
                .password("Password123!")
                .build();

        AuthenticationResponse response = authenticationService.authenticate(request);

        // Token JWT présent
        assertNotNull(response.getToken());

        // Ouverture du JWT
        String payload = new String(Base64.getDecoder()
                .decode(response.getToken().split("\\.")[1]));

        // Vérification que l'email et le rôle sont bien dans le payload
        assertTrue(payload.contains("user@bookhub.fr"));
        assertTrue(payload.contains("USER"));
    }
}
