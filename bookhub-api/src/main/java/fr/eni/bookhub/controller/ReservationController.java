package fr.eni.bookhub.controller;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.bll.ReservationService;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@AllArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private ReservationService reservationService;
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> reserve(
            @RequestParam("bookId") String bookIdParam,
            Principal principal
    ) {
        try {
            int bookId = Integer.parseInt(bookIdParam);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            reservationService.reserve(user.getId(), bookId);
            return ResponseEntity.ok("Réservation créée");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations(
            Principal principal
    ) {
        return ResponseEntity.ok(reservationService.getUserReservations(principal.getName()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(
            @PathVariable("id") int reservationId
    ) {
        reservationService.cancel(reservationId);

        // 204 No content si ok
        return ResponseEntity.noContent().build();
    }
}
