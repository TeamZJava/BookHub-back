package fr.eni.bookhub.controller;

import fr.eni.bookhub.bll.AuthenticationService;
import fr.eni.bookhub.bll.UserService;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dto.*;
import fr.eni.bookhub.security.jwt.AuthenticationRequest;
import fr.eni.bookhub.security.jwt.AuthenticationResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final AuthenticationService authenticationService;

    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.add(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserUpdateResponse> update(
            @PathVariable int id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // Delete — DELETE /api/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        userService.delete(id);
        // 204 No Content = succès sans corps de réponse, standard REST pour un delete
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable int id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }


}