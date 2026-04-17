package fr.eni.bookhub.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    // La validation du pattern reste ici, pas sur l'entité
    @NotBlank(message = "Mot de passe obligatoire")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{12,}$",
            message = "Le mot de passe doit contenir au moins 12 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial."
    )
    private String password;

    @NotBlank(message = "Prénom obligatoire")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Nom obligatoire")
    @Size(max = 100)
    private String lastName;

    @Size(max = 12)
    private String phone;

    @NotNull(message = "Vous devez accepter les CGU")
    private LocalDateTime tosAcceptationDate;
}