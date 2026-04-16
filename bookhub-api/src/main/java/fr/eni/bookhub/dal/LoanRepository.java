package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Loan;
import fr.eni.bookhub.bo.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    List<Loan> findByUserId(Integer userId);

    List<Loan> findByBookId(Integer bookId);

    List<Loan> findByStatus(LoanStatus status);

    boolean existsByBookIdAndStatus(Integer bookId, LoanStatus status);

}
