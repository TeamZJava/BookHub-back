package fr.eni.bookhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    // DTO qui sert à renvoyer du JSON dans FavoriteController au lieu de texte brut
    private String message;
}

