package fr.eni.bookhub.bll;

import static org.junit.jupiter.api.Assertions.*;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.authentification.RegisterRequest;
import fr.eni.bookhub.errors.BadRequestException;
import fr.eni.bookhub.errors.NotFoundException;
import fr.eni.bookhub.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@Transactional
public class UserServiceDeleteTest {
    @Autowired
    private UserRepository userRepository;

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


    // Suppression réussie
    @Test
    void delete_user_ok() {

        assertTrue(userRepository.existsById(userId));

        userService.delete(userId);

        assertThrows(NotFoundException.class, () -> userService.findById(userId));
    }

    // ID invalide
    @Test
    void delete_user_invalid_id() {

        assertThrows(BadRequestException.class, () -> userService.delete(-2));

    }

    // Suppression d'un utilisateur inexistant
    @Test
    void delete_user_not_exist() {

        assertThrows(BadRequestException.class, () -> userService.delete(12));

    }
}
