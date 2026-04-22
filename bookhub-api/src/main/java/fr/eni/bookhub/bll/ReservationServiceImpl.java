package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Reservation;
import fr.eni.bookhub.bo.enums.ReservationStatus;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.ReservationRepository;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.reservation.ReservationDTO;
import fr.eni.bookhub.errors.BadRequestException;
import fr.eni.bookhub.errors.NotFoundException;
import fr.eni.bookhub.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {

    private final UserMapper userMapper;
    private ReservationRepository reservationRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    @Override
    public List<ReservationDTO> getUserReservations(String email) {
        // Récupération user
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Utilisateur non trouvé")
        );

        // Remplissage d'un tableau avec toute les résas de l'utilisateur
        List<Reservation> reservations = reservationRepository.findByUserId(user.getId());

        return reservations.stream()
                .map(userMapper::toReservationDTO)
                .toList();
    }

    @Transactional
    @Override
    public void reserve(int userId, int bookId) {

        // Récupération du livre
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Livre introuvable"));

        // Récupération de l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // Calcul du rang (dernier de la liste)
        int rang = reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.PENDING) + 1;

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .reservationDate(LocalDateTime.now())
                .rankInLine(rang)
                .status(ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    @Override
    public void cancel(int reservationId) {
        if (reservationId <= 0) {
            throw new BadRequestException("ID invalide");
        }

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundException("Reservation introuvable")
        );

        // Passage en CANCELED, pas de suppression de la resa
        reservation.setStatus(ReservationStatus.CANCELED);

        reservationRepository.save(reservation);

        // Ajuste les numéros dans la file
        reorderReservations(reservation.getBook().getId());
    }


    @Transactional
    protected void reorderReservations(int bookId) {

        // Récupération des réservations liées au livre
        List<Reservation> reservations = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.PENDING);

        int rank = 1;

        // Recalcul du rang sur chaque réservation
        for (Reservation reservation : reservations) {
            reservation.setRankInLine(rank);
            rank++;
        }

        // Sauvegarde de toute les réservations
        reservationRepository.saveAll(reservations);
    }

}
