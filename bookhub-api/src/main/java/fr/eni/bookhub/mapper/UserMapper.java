package fr.eni.bookhub.mapper;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dto.authentification.RegisterRequest;
import fr.eni.bookhub.dto.authentification.RegisterResponse;
import fr.eni.bookhub.dto.authentification.UserResponse;
import fr.eni.bookhub.dto.authentification.UserUpdateResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // RegisterRequest -> User
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)        // forcé à USER dans la BLL
    @Mapping(target = "active", ignore = true)      // forcé à true dans la BLL
    @Mapping(target = "inscriptionDate", ignore = true) // now() dans la BLL
    @Mapping(target = "password", ignore = true)    // Hashé avec bcrypt dans la BLL
    User toEntity(RegisterRequest request);

    // User -> RegisterResponse
    // role est un enum, on le convertit en String avec .name()
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    RegisterResponse toRegisterResponse(User user);

    // User -> UserResponse (pour les GET /users/**)
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toUserResponse(User user);

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserUpdateResponse toUpdateResponse(User user);
}