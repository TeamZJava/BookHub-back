package fr.eni.bookhub.bll;

import fr.eni.bookhub.dto.authentification.*;

import java.util.List;

public interface UserService {
    RegisterResponse add(RegisterRequest user);

    UserUpdateResponse update(int id, UserUpdateRequest request);

    void delete(int id);

    UserResponse findById(int id);

    List<UserResponse> findAll();

    UserResponse getUserProfile(String email);

    // méthodes pour update le rôle pour ADMIN
}
