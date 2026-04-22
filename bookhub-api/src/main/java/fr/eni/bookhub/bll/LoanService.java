package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Loan;
import fr.eni.bookhub.dto.emprunts.LoanDTO;

import java.util.List;

public interface LoanService {
    List<LoanDTO> getUserLoans(String email);

    // TODO : Retour d'un livre
//    void finishLoan();

    List<LoanDTO> getAllLoans();

    void borrow(int userId, int bookId);

    boolean isLate(int userId);
}
