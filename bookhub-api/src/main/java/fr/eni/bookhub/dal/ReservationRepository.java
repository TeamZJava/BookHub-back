package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Reservation;
import fr.eni.bookhub.bo.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByBookId(Integer bookId);

    int countByBookIdAndStatus(Integer bookId, ReservationStatus status);

    // Récupère les réservation d'un livre et les tris par date croissante
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Integer bookId, ReservationStatus status);
}
