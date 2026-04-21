package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Loan;

import java.util.List;

public interface LoanService {
    List<Loan> getUserLoans(String email);

    // TODO : Retour d'un livre
//    void finishLoan();

    List<Loan> getAllLoans();

    void borrow(int userId, int bookId);

    boolean isLate(int userId);
}
