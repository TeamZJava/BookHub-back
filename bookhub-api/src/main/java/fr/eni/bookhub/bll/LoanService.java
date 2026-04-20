package fr.eni.bookhub.bll;

public interface LoanService {

    void borrow(int userId, int bookId);

    boolean isLate(int userId);
}
