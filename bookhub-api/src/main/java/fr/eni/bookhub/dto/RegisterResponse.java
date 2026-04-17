package fr.eni.bookhub.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}