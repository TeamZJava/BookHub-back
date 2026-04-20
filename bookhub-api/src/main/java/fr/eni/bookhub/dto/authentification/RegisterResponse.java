package fr.eni.bookhub.dto.authentification;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}