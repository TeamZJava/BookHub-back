package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByBookId(Integer bookId);
}
