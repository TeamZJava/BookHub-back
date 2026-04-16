package fr.eni.bookhub.controller;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.bll.FavoriteService;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private FavoriteService favoriteService;
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getFavorites(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            List<Book> favoris = favoriteService.getFavorites(user.getId());
            if (favoris == null || favoris.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(favoris);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<?> addFavorite(@PathVariable("bookId") String bookIdInPath, Principal principal) {
        try {
            int bookId = Integer.parseInt(bookIdInPath);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            favoriteService.addFavorite(user.getId(), bookId);
            return ResponseEntity.ok("Favori ajouté");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> removeFavorite(@PathVariable("bookId") String bookIdInPath, Principal principal) {
        try {
            int bookId = Integer.parseInt(bookIdInPath);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            favoriteService.removeFavorite(user.getId(), bookId);
            return ResponseEntity.ok("Favori supprimé");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @GetMapping("/check/{bookId}")
    public ResponseEntity<?> isFavorite(@PathVariable("bookId") String bookIdInPath, Principal principal) {
        try {
            int bookId = Integer.parseInt(bookIdInPath);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            return ResponseEntity.ok(favoriteService.isFavorite(user.getId(), bookId));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
}
