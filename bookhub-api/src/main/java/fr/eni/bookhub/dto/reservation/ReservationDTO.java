package fr.eni.bookhub.dto.reservation;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.enums.ReservationStatus;
import fr.eni.bookhub.dto.authentification.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private int id;
    private UserResponse user;
    private Book book;
    private LocalDateTime reservationDate;
    private int rankInLine;
    private ReservationStatus status;
}
