package fr.eni.bookhub.dto.authentification;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "Email obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Prénom obligatoire")
    @Size(max = 100, message = "Le prénom ne peut excéder 100 caractères")
    private String firstName;

    @NotBlank(message = "Nom obligatoire")
    @Size(max = 100, message = "Le nom ne peut excéder 100 caractères")
    private String lastName;

    @Size(max = 12, message = "Le numéro de téléphone doit faire moins de 12 caractères")
    private String phone;

    private String oldPassword;

    // Nullable — si non fourni, on conserve l'ancien mot de passe
    @Pattern(
            regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{12,}$",
            message = "Le mot de passe doit contenir au moins 12 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial."
    )
    private String password;
}