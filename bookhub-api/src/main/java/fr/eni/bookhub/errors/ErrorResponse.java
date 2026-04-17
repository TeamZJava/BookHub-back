package fr.eni.bookhub.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private List<String> errors;
}