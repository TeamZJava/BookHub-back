package fr.eni.bookhub.controller;

import fr.eni.bookhub.bo.Loan;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.bll.LoanService;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.emprunts.LoanDTO;
import fr.eni.bookhub.errors.ForbiddenException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private LoanService loanService;
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> findAll(
            Authentication authentication
    ) {
        if(authentication.getAuthorities()
                .stream()
                .noneMatch(
                a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_LIBRARIAN"))) {
            throw new ForbiddenException("Vous n'avez pas les droits pour accéder à cette page");
        }
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/is-late")
    public ResponseEntity<Boolean> hasOverdue(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            return ResponseEntity.ok(loanService.isLate(user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(false);
        }
    }

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

    @GetMapping("/my")
    public ResponseEntity<List<LoanDTO>> getMyLoans(
            Principal principal
    ) {
        return ResponseEntity.ok(loanService.getUserLoans(principal.getName()));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(
            @PathVariable("id") int loanId,
            Authentication authentication
    ) {
        if(authentication.getAuthorities()
                .stream()
                .noneMatch(
                        a -> a.getAuthority().equals("ROLE_ADMIN")
                                || a.getAuthority().equals("ROLE_LIBRARIAN"))) {
            throw new ForbiddenException("Vous n'avez pas les droits pour accéder à cette page");
        }

        loanService.finishLoan(loanId);

        // 204 No Content
        return ResponseEntity.noContent().build();
    }
}
