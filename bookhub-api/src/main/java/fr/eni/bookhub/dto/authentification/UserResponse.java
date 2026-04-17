package fr.eni.bookhub.dto.authentification;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private boolean active;
}