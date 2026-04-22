package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.enums.Role;
import fr.eni.bookhub.dto.authentification.*;

import java.util.List;

public interface UserService {
    RegisterResponse add(RegisterRequest user);

    UserUpdateResponse update(int id, UserUpdateRequest request);

    void delete(int id);

    UserResponse findById(int id);

    List<UserResponse> findAll();

    UserResponse getUserProfile(String email);

    UserResponse setRole(int userId, Role role);
}
