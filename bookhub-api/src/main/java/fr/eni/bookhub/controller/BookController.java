package fr.eni.bookhub.controller;

import fr.eni.bookhub.bll.BookService;
import fr.eni.bookhub.bll.CommentService;
import fr.eni.bookhub.bll.RatingService;
import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.controller.dto.BookDetailDTO;
import fr.eni.bookhub.dal.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService bookService;
    private CommentService commentService;
    private RatingService ratingService;
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<Book> livres = bookService.search(search, category, available, pageable);
        if (livres == null || livres.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(livres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable("id") String idInPath, Principal principal) {
        try {
            int id = Integer.parseInt(idInPath);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            BookDetailDTO detail = bookService.getDetail(id, user.getId());
            return ResponseEntity.ok(detail);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<String> categories = bookService.getCategories();
        if (categories == null || categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody Book book) {
        try {
            bookService.create(book);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable("id") String idInPath, @Valid @RequestBody Book book) {
        try {
            int id = Integer.parseInt(idInPath);
            return ResponseEntity.ok(bookService.update(id, book));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable("id") String idInPath) {
        try {
            int id = Integer.parseInt(idInPath);
            bookService.delete(id);
            return ResponseEntity.ok("Livre (" + id + ") supprimé");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/ratings")
    public ResponseEntity<?> rateBook(@PathVariable("id") String idInPath,
                                      @RequestParam String note,
                                      Principal principal) {
        try {
            int id = Integer.parseInt(idInPath);
            int noteInt = Integer.parseInt(note);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            ratingService.rateBook(user.getId(), id, noteInt);
            return ResponseEntity.ok("Note ajoutée");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable("id") String idInPath,
                                        @RequestBody Map<String, String> body,
                                        Principal principal) {
        try {
            int id = Integer.parseInt(idInPath);
            User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            commentService.addComment(user.getId(), id, body.get("comment"));
            return ResponseEntity.ok("Commentaire ajouté");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Votre identifiant n'est pas un entier");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
}
