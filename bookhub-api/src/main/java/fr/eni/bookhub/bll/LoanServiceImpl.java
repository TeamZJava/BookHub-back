package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Loan;
import fr.eni.bookhub.bo.enums.LoanStatus;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.LoanRepository;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.emprunts.LoanDTO;
import fr.eni.bookhub.errors.BadRequestException;
import fr.eni.bookhub.errors.NotFoundException;
import fr.eni.bookhub.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository loanRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<LoanDTO> getUserLoans(String email) {

        // Récupération + vérif user en base
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("L'utilisateur n'existe pas")
        );

        // On remplit le tableau avec tout les emprunts liés à l'utilisateur
        List<Loan> loans = loanRepository.findByUserId(user.getId());

        // Filtre sur le status : on récupère seulement les emprunts actifs ou en retard
        loans.stream()
                .filter(
                        loan -> loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE
                )
                .toList();

        return loans.stream()
                .map(userMapper::toLoanDto)
                .toList();
    }

    @Override
    public void finishLoan(int loanId) {
        if(loanId <= 0) {
            throw new BadRequestException("ID invalide");
        }

        // Récupère l'emprunt
        Loan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new NotFoundException("Aucun emprunt avec cet ID")
        );

        Book bookTiedtoTheLoan = bookRepository.findById(loan.getBook().getId()).orElseThrow(
                () -> new NotFoundException("Aucun livre avec cet ID")
        );

        // Passage à l'état rendu
        loan.setStatus(LoanStatus.RETURNED);

        // Date de retour
        loan.setReturnDate(LocalDateTime.now());

        // +1 copie disponible
        bookTiedtoTheLoan.setAvailableCopies(bookTiedtoTheLoan.getAvailableCopies() + 1);

        loanRepository.save(loan);
        bookRepository.save(bookTiedtoTheLoan);
    }

    @Override
    public List<LoanDTO> getAllLoans() {
        // On remplit le tableau avec tout les emprunts
        List<Loan> loans = loanRepository.findAll();

        // Filtre sur le status : on récupère seulement les emprunts actifs ou en retard
        loans.stream()
                .filter(
                        loan -> loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE
                )
                .toList();

        return loans.stream()
                .map(userMapper::toLoanDto)
                .toList();
    }


    @Override
    public void borrow(int userId, int bookId) {
        final Book book = validerLivre(bookId);

        // Vérifier la dispo
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Aucun exemplaire disponible pour ce livre");
        }

        final User utilisateur = validerUtilisateur(userId);

        // Créer l'emprunt
        final Loan emprunt = new Loan();
        emprunt.setUser(utilisateur);
        emprunt.setBook(book);
        emprunt.setLoanDate(LocalDateTime.now());
        emprunt.setDueDate(LocalDateTime.now().plusDays(14));
        emprunt.setStatus(LoanStatus.ACTIVE);

        // Décrémenter les exemplaires disponibles
        book.setAvailableCopies(book.getAvailableCopies() - 1);

        try {
            bookRepository.save(book);
            loanRepository.save(emprunt);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer l'emprunt !");
        }
    }

    @Override
    public boolean isLate(int userId) {
        List<Loan> emprunts = loanRepository.findByUserId(userId);
        for (Loan emprunt : emprunts) {
            if (emprunt.getStatus() == LoanStatus.ACTIVE && emprunt.getDueDate().isBefore(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    private Book validerLivre(int bookId) {
        if (bookId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas");
        }
        final Optional<Book> opt = bookRepository.findById(bookId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun livre ne correspond à l'identifiant " + bookId);
    }

    private User validerUtilisateur(int userId) {
        if (userId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas !");
        }
        final Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun utilisateur ne correspond à l'identifiant " + userId);
    }
}
