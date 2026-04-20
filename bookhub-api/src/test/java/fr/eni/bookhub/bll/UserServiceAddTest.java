package fr.eni.bookhub.bll;

import static org.junit.jupiter.api.Assertions.*;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.authentification.RegisterRequest;
import fr.eni.bookhub.dto.authentification.RegisterResponse;
import fr.eni.bookhub.errors.BadRequestException;
import fr.eni.bookhub.errors.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
public class UserServiceAddTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImpl userService;

    private RegisterRequest request;

    @BeforeEach
    void setup() {
        request = RegisterRequest.builder()
                .email("user@bookhub.fr")
                .password("Password123!")
                .firstName("UserFirstName")
                .lastName("UserLastName")
                .tosAcceptationDate(LocalDateTime.now())
                .build();
    }


    // Inscription réussie
    @Test
    void register_passed() {

        RegisterResponse response = userService.add(request);

        // Existe en base
        assertTrue(userRepository.findByEmail(request.getEmail()).isPresent());

        // Vérifier que les informations correspondent
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
    }

    // Email déjà utilisé
    @Test
    void register_email_already_used() {

        userService.add(request);

        RegisterRequest userToRegister = RegisterRequest.builder()
                .email("user@bookhub.fr")
                .password("Password123!!")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .tosAcceptationDate(LocalDateTime.now())
                .build();

        // Lance bien une erreur conflict
        assertThrows(ConflictException.class, () -> userService.add(userToRegister));
    }

    // CGU non acceptées
    @Test
    void register_tos_not_accepted() {

        // Retrait de la date d'acceptation des CGU
        request.setTosAcceptationDate(null);

        // Lance bien une erreur conflict
        assertThrows(BadRequestException.class, () -> userService.add(request));
    }

    // Rôle bien initialisé à USER
    @Test
    void register_test_role() {

        RegisterResponse response = userService.add(request);

        assertNotNull(response);

        assertEquals("USER", response.getRole());
    }


    // Le mdp est bien haché
    @Test
    void register_password_is_hashed() {
        userService.add(request);

        User savedUser = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // Le mdp censé être hashé ne correspond pas au mdp en clair
        assertNotEquals(request.getPassword(), savedUser.getPassword());

        // Le mdp décrypté doit correspondre au mot de passe en clair
        assertTrue(passwordEncoder.matches(request.getPassword(), savedUser.getPassword()));
    }

}
