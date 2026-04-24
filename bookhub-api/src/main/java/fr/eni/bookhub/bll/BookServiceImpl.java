package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.*;
import fr.eni.bookhub.bo.enums.LoanStatus;
import fr.eni.bookhub.dto.catalogue.BookDetailDTO;
import fr.eni.bookhub.dto.catalogue.CommentDTO;
import fr.eni.bookhub.dal.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private BookRepository bookRepository;
    private CommentRepository commentRepository;
    private RatingRepository ratingRepository;
    private FavoriteRepository favoriteRepository;
    private LoanRepository loanRepository;

    @Override
    public Page<Book> search(String search, String category, Boolean available, Pageable pageable) {
        return bookRepository.search(search, category, available, pageable);
    }

    @Override
    public BookDetailDTO getDetail(int bookId, int userId) {
        final Book book = chargerLivreParId(bookId);

        final List<Comment> comments = commentRepository.findByBookId(bookId);
        final List<Rating> ratings = ratingRepository.findByBookId(bookId);

        // Construire note user a donnée au livre (pour enrichir chaque commentaire)
        final Map<Integer, Integer> ratingParUser = ratings.stream()
                .collect(Collectors.toMap(
                        rating -> rating.getUser().getId(),
                        Rating::getNote,
                        (note1, note2) -> note2));

        // construire la liste de commentDTO
        final List<CommentDTO> commentDTOs = comments.stream()
                .map(comment -> new CommentDTO(
                        comment.getId(),
                        comment.getUser().getFirstName(),
                        comment.getUser().getLastName(),
                        comment.getComment(),
                        comment.getCommentDate(),
                        ratingParUser.get(comment.getUser().getId()),
                        comment.getReported()))
                .collect(Collectors.toList());

        final boolean estFavori = favoriteRepository.existsByUserIdAndBookId(userId, bookId);

        // Note personnelle de l'userconnecté
        Integer noteUtilisateur = null;
        final Optional<Rating> noteOpt = ratingRepository.findByUserIdAndBookId(userId, bookId);
        if (noteOpt.isPresent()) {
            noteUtilisateur = noteOpt.get().getNote();
        }

        return new BookDetailDTO(book, commentDTOs, estFavori, noteUtilisateur);
    }

    @Override
    public Book create(Book book) {
        if (book == null) {
            throw new RuntimeException("Le livre est obligatoire !");
        }
        validerLivre(book);
        book.setAddedAt(LocalDateTime.now());
        book.setAvgRating(BigDecimal.ZERO);
        try {
            final Book bookDB = bookRepository.save(book);
            return bookDB;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer le livre");
        }
    }

    @Override
    public Book update(int id, Book book) {
        if (book == null) {
            throw new RuntimeException("Le livre est obligatoire");
        }
        final Book livreExistant = chargerLivreParId(id);
        livreExistant.setTitle(book.getTitle());
        livreExistant.setAuthor(book.getAuthor());
        livreExistant.setIsbn(book.getIsbn());
        livreExistant.setDescription(book.getDescription());
        livreExistant.setCategory(book.getCategory());
        livreExistant.setCoverUrl(book.getCoverUrl());
        livreExistant.setTotalCopies(book.getTotalCopies());
        livreExistant.setAvailableCopies(book.getAvailableCopies());
        try {
            final Book bookDB = bookRepository.save(livreExistant);
            return bookDB;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de modifier le livre (id = " + id + ")");
        }
    }

    @Override
    public void delete(int id) {
        if (id <= 0) {
            throw new RuntimeException("Identifiant n'existe pas !");
        }
        if (loanRepository.existsByBookIdAndStatus(id, LoanStatus.ACTIVE)) {
            throw new RuntimeException("Impossible de supprimer un livre avec des emprunts en cours");
        }
        try {
            bookRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de supprimer le livre (id = " + id + ")");
        }
    }

    //recup categories
    @Override
    public List<String> getCategories() {
        return bookRepository.findAllCategories();
    }

    private Book chargerLivreParId(int id) {
        if (id <= 0) {
            throw new RuntimeException("Identifiant n'existe pas !");
        }
        final Optional<Book> opt = bookRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun livre ne correspond à l'identifiant " + id);
    }

    private void validerLivre(Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new RuntimeException("Le titre est obligatoire");
        }
        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            throw new RuntimeException("L'auteur est obligatoire");
        }
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new RuntimeException("L'ISBN est obligatoire");
        }
         if (book.getTotalCopies() <= 0) {
            throw new RuntimeException("Le nombre d'exemplaires doit être positif");
        }
        if (book.getAvailableCopies() < 0) {
            throw new RuntimeException("Le nombre d'exemplaires disponibles ne peut pas être négatif");
        }
    }
}
