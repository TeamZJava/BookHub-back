package fr.eni.bookhub.controller;

import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.bll.LoanService;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@AllArgsConstructor
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private LoanService loanService;
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> borrow(@RequestParam("bookId") String bookIdParam, Principal principal) {
        try {
            int bookId = Integer.parseInt(bookIdParam);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé..."));
            loanService.borrow(user.getId(), bookId);
            return ResponseEntity.ok("Emprunt créé !");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
}
