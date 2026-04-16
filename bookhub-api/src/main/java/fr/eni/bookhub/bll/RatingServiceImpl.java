package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Rating;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.RatingRepository;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class RatingServiceImpl implements RatingService {

    private RatingRepository ratingRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    @Override
    public void rateBook(int userId, int bookId, int note) {
        if (note < 1 || note > 5) {
            throw new RuntimeException("La note doit être comprise entre 1 et 5 !");
        }

        final Optional<Rating> noteExistante = ratingRepository.findByUserIdAndBookId(userId, bookId);

        if (noteExistante.isPresent()) {
            // L'utilisateur a déjà noté ce livre : faut mettre à jour
            final Rating rating = noteExistante.get();
            rating.setNote(note);
            rating.setRatingDate(LocalDateTime.now());
            try {
                ratingRepository.save(rating);
            } catch (Exception e) {
                throw new RuntimeException("Impossible de mettre à jour la note !");
            }
        } else {
            // Première note de cet utilisateur pour ce livre
            final User user = validerUtilisateur(userId);
            final Book book = validerLivre(bookId);

            final Rating rating = new Rating();
            rating.setUser(user);
            rating.setBook(book);
            rating.setNote(note);
            rating.setRatingDate(LocalDateTime.now());
            try {
                ratingRepository.save(rating);
            } catch (Exception e) {
                throw new RuntimeException("Impossible d'enregistrer la note !");
            }
        }

        recalculerMoyenne(bookId);
    }

    private void recalculerMoyenne(int bookId) {
        final List<Rating> toutesLesNotes = ratingRepository.findByBookId(bookId);

        final Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (!bookOpt.isPresent()) {
            return;
        }
        final Book book = bookOpt.get();

        if (toutesLesNotes.isEmpty()) {
            book.setAvgRating(BigDecimal.ZERO);
        } else {
            int total = 0;
            for (Rating rating : toutesLesNotes) {
                total += rating.getNote();
            }
            final double moyenne = (double) total / toutesLesNotes.size();
            book.setAvgRating(BigDecimal.valueOf(moyenne).setScale(2));
        }

        try {
            bookRepository.save(book);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de recalculer la moyenne !");
        }
    }

    private User validerUtilisateur(int userId) {
        if (userId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas !");
        }
        final Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun utilisateur ne correspond à l'identifiant " + userId);
    }

    private Book validerLivre(int bookId) {
        if (bookId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas !");
        }
        final Optional<Book> opt = bookRepository.findById(bookId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun livre ne correspond à l'identifiant " + bookId);
    }
}
