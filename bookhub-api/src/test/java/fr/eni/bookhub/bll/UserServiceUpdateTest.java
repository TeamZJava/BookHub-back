package fr.eni.bookhub.bll;

import static org.junit.jupiter.api.Assertions.*;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.authentification.RegisterRequest;
import fr.eni.bookhub.dto.authentification.UserUpdateRequest;
import fr.eni.bookhub.dto.authentification.UserUpdateResponse;
import fr.eni.bookhub.errors.BadRequestException;
import fr.eni.bookhub.errors.ConflictException;
import fr.eni.bookhub.errors.NotFoundException;
import fr.eni.bookhub.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@Transactional
public class UserServiceUpdateTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    UserMapper userMapper;

    private int userId;

    // Création d'un utilisateur avant chaque test pour avoir quelqu'un à modifier
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

        Optional<User> user = userRepository.findByEmail(userToConnect.getEmail());

        userId = user.get().getId();
    }

    // Modification réussie sans changement mdp
    @Test
    void update_without_password_change() {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newuseremail@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .phone("0601020304")
                .build();

        UserUpdateResponse response = userService.update(userId, request);

        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
        assertEquals(request.getPhone(), response.getPhone());
    }

    // Vérif que le mot de passe n'est pas modifié après un changement sans le spécifier
    @Test
    void update_password_unchanged_when_not_provided() {
        // Récupère le hash du mdp
        String passwordAvantUpdate = userRepository.findById(userId)
                .orElseThrow()
                .getPassword();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newuseremail@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .build();

        userService.update(userId, request);

        // Récupère le hash du mdp après la modification
        String passwordApresUpdate = userRepository.findById(userId)
                .orElseThrow()
                .getPassword();

        // Vérification que le mdp n'est pas bougé
        assertEquals(passwordAvantUpdate, passwordApresUpdate);
    }

    // Modification réussie AVEC changement mdp
    @Test
    void update_with_password() {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newuseremail@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .oldPassword("Password123!")
                .password("NewPassword123!")
                .build();

        userService.update(userId, request);


        User userApresUpdate = userRepository.findById(userId).orElseThrow();

        // Vérifie que le nouveau mdp correspond à celui en base
        assertTrue(passwordEncoder.matches("NewPassword123!", userApresUpdate.getPassword()));

        // Vérifie que l'ancien mdp ne correspond plus à celui en base
        assertFalse(passwordEncoder.matches("Password123!", userApresUpdate.getPassword()));
    }

    // ID invalide
    @Test
    void update_invalid_id() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newuseremail@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .oldPassword("Password123!")
                .password("NewPassword123!")
                .build();

        // On a bien une erreur de mauvaise requête
        assertThrows(BadRequestException.class, () -> userService.update(-2, request));
    }

    // Utilisateur introuvable
    @Test
    void update_user_not_found() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newuseremail@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .oldPassword("Password123!")
                .password("NewPassword123!")
                .build();

        // On a bien une erreur de non trouvé
        assertThrows(NotFoundException.class, () -> userService.update(5, request));
    }

    // Nouvel email déjà pris
    @Test
    void update_email_already_taken() {
        RegisterRequest userToConnect = RegisterRequest.builder()
                .email("newuseremail@bookhub.fr")
                .password("Password123!")
                .firstName("UserFirstName")
                .lastName("UserLastName")
                .tosAcceptationDate(LocalDateTime.now())
                .build();

        userService.add(userToConnect);

        Optional<User> user = userRepository.findByEmail(userToConnect.getEmail());

        int newId = user.get().getId();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("user@bookhub.fr")
                .firstName("UserFirstName")
                .lastName("UserLastName")
                .build();

        // On a bien une erreur de conflit
        assertThrows(ConflictException.class, () -> userService.update(newId, request));
    }

    // Nouveau mdp fourni sans ancien mdp
    @Test
    void update_password_without_old_password() {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("user@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .password("NewPassword123!")
                .build();

        // Remonte bien une mauvaise requête
        assertThrows(BadRequestException.class, () -> userService.update(userId, request));
    }

    // Ancien mot de passe incorrect
    @Test
    void update_password_wrong_old_password() {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("user@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .oldPassword("Password123@")
                .password("NewPassword123!")
                .build();

        // Remonte bien une mauvaise requête
        assertThrows(BadRequestException.class, () -> userService.update(userId, request));
    }

    // Token bien généré lors d'une update
    @Test
    void update_should_return_token() {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newuseremail@bookhub.fr")
                .firstName("UserFirstNameBis")
                .lastName("UserLastNameBis")
                .build();

        UserUpdateResponse response = userService.update(userId, request);

        // Vérifie que le token est généré
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }

}