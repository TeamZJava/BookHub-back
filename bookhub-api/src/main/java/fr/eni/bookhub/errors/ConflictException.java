package fr.eni.bookhub.errors;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
