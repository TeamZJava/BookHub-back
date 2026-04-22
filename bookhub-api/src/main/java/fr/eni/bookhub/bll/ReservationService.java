package fr.eni.bookhub.bll;

import fr.eni.bookhub.dto.emprunts.LoanDTO;
import fr.eni.bookhub.dto.reservation.ReservationDTO;

import java.util.List;

public interface ReservationService {

    List<ReservationDTO> getUserReservations(String email);

    void reserve(int userId, int bookId);

    void cancel(int reservationId);
}
