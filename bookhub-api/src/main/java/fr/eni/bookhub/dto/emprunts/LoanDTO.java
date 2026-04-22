package fr.eni.bookhub.dto.emprunts;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.enums.LoanStatus;
import fr.eni.bookhub.dto.authentification.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private int id;
    private UserResponse user;
    private Book book;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;
}
