package fr.eni.bookhub.bll;

import fr.eni.bookhub.dto.emprunts.LoanDTO;

import java.util.List;

public interface LoanService {
    List<LoanDTO> getUserLoans(String email);

    // Retour d'un livre
    void finishLoan(int loanId);

    List<LoanDTO> getAllLoans();

    void borrow(int userId, int bookId);

    boolean isLate(int userId);
}
