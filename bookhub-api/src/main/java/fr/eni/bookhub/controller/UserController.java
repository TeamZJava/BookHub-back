package fr.eni.bookhub.controller;

import fr.eni.bookhub.bll.AuthenticationService;
import fr.eni.bookhub.bll.UserService;
import fr.eni.bookhub.bo.enums.Role;
import fr.eni.bookhub.dto.authentification.*;
import fr.eni.bookhub.errors.ForbiddenException;
import fr.eni.bookhub.security.jwt.AuthenticationRequest;
import fr.eni.bookhub.security.jwt.AuthenticationResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final AuthenticationService authenticationService;

    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid
            @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = userService.add(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/users")
    public ResponseEntity<UserUpdateResponse> update(
            Principal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.getUserProfile(principal.getName());

        return ResponseEntity.ok(userService.update(user.getId(), request));
    }

    // Delete — DELETE /api/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable int id
    ) {
        userService.delete(id);
        // 204 No Content
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable int id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/users/my")
    public ResponseEntity<UserResponse> getMyProfil(
            Principal principal
    ) {
        return ResponseEntity.ok(userService.getUserProfile(principal.getName()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> modifyRole(
            @PathVariable int id,
            @RequestParam("role") String role,
            Authentication authentication
    ) {
        if(authentication.getAuthorities()
                .stream()
                .noneMatch(
                        a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ForbiddenException("Vous n'avez pas les droits pour faire cette requête");
        }

        return ResponseEntity.ok(userService.setRole(id, Role.valueOf(role)));
    }

    @PostMapping("/users/{id}/active")
    public ResponseEntity<?> toggleActive(
            @PathVariable int id,
            Authentication authentication
    ) {
        if(authentication.getAuthorities()
                .stream()
                .noneMatch(
                        a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ForbiddenException("Vous n'avez pas les droits pour faire cette requête");
        }

        userService.setActive(id);

        // 204
        return ResponseEntity.noContent().build();
    }
}