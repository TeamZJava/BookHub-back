package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Reservation;
import fr.eni.bookhub.bo.ReservationStatus;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.ReservationRepository;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private ReservationRepository reservationRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    @Override
    public void reserve(int userId, int bookId) {
        final Book book = validerLivre(bookId);
        final User utilisateur = validerUtilisateur(userId);

        // Calculer le rang dans la file d'attente
        final List<Reservation> reservationsExistantes = reservationRepository.findByBookId(bookId);
        int rang = 0;
        for (Reservation r : reservationsExistantes) {
            if (r.getStatus() == ReservationStatus.PENDING) {
                rang++;
            }
        }
        rang = rang + 1;

        // Créer la réservation
        final Reservation reservation = new Reservation();
        reservation.setUser(utilisateur);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setRankInLine(rang);
        reservation.setStatus(ReservationStatus.PENDING);

        try {
            reservationRepository.save(reservation);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer la réservation !");
        }
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
            throw new RuntimeException("Identifiant n'existe pas");
        }
        final Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun utilisateur ne correspond à l'identifiant " + userId);
    }
}
