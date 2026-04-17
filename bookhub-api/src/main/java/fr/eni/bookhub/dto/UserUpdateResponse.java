package fr.eni.bookhub.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
}