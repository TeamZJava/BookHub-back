package fr.eni.bookhub.security.jwt;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "email")
public class AuthenticationRequest {
    private String email;
    private String password;
}