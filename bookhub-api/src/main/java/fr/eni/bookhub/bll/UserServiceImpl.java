package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.enums.Role;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.authentification.*;
import fr.eni.bookhub.errors.BadRequestException;
import fr.eni.bookhub.errors.ConflictException;
import fr.eni.bookhub.errors.NotFoundException;
import fr.eni.bookhub.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Transactional
    @Override
    public RegisterResponse add(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        if (request.getTosAcceptationDate() == null) {
            throw new BadRequestException("Vous devez accepter les CGU");
        }

        // MapStruct convertit le DTO en entité (email, firstName, lastName, phone, tos...)
        User user = userMapper.toEntity(request);

        // force les valeurs que le client ne doit pas pouvoir définir
        user.setRole(Role.USER);
        user.setActive(true);
        user.setInscriptionDate(LocalDateTime.now());

        // Hachage du mot de passe
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Sauvegarde
        User savedUser = userRepository.save(user);

        // Conversion vers le DTO de réponse
        RegisterResponse response = userMapper.toRegisterResponse(savedUser);

        return response;
    }

    @Transactional
    @Override
    public UserUpdateResponse update(int id, UserUpdateRequest request) {

        // Vérification ID
        if (id < 1) {
            throw new BadRequestException("ID invalide");
        }

        // Vérification existence en base
        User userEnBase = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // Vérification email
        if (!userEnBase.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        // Mise à jour uniquement des champs autorisés
        userEnBase.setEmail(request.getEmail());
        userEnBase.setFirstName(request.getFirstName());
        userEnBase.setLastName(request.getLastName());
        userEnBase.setPhone(request.getPhone());

        // Mot de passe seulement si l'utilisateur en envoie un nouveau
        if (request.getPassword() != null && !request.getPassword().isBlank()) {

            // L'ancien mot de passe est obligatoire si on veut en définir un nouveau
            if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
                throw new BadRequestException("L'ancien mot de passe est obligatoire pour en définir un nouveau");
            }

            // Vérifie que l'ancien mot de passe correspond bien à celui en base
            if (!passwordEncoder.matches(request.getOldPassword(), userEnBase.getPassword())) {
                throw new BadRequestException("L'ancien mot de passe est incorrect");
            }

            // Hachage du mdp
            userEnBase.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Sauvegarde
        User savedUser = userRepository.save(userEnBase);

        // Conversion vers le DTO
        UserUpdateResponse response = userMapper.toUpdateResponse(savedUser);

        // Nouveau token
        response.setToken(jwtService.generateToken(savedUser));

        return response;
    }

    @Override
    public void delete(int id) {
        if(id < 1) {
            throw new BadRequestException("L'id doit être supérieur à zéro");
        }

        if (!userRepository.existsById(id)) {
            throw new BadRequestException("Utilisateur introuvable avec l'id : " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public UserResponse findById(int id) {
        if (id < 1) {
            throw new BadRequestException("L'ID doit être supérieur à zéro");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aucun utilisateur trouvé avec cet ID"));

        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Aucun utilisateur trouvé avec cet email"));

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse setRole(int userId, Role role) {
        if (userId <= 0) {
            throw new BadRequestException("ID invalide");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Utilisateur non trouvé")
        );

        user.setRole(role);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Override
    public void setActive(int userId) {
        if (userId <= 0) {
            throw new BadRequestException("ID invalide");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Utilisateur non trouvé")
        );

        // Inverse le booléen pour activer ou désactiver le rôle
        user.setActive(!user.isActive());

        userRepository.save(user);
    }

}
