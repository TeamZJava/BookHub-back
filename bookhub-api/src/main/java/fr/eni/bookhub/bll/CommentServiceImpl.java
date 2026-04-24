package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Comment;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.CommentRepository;
import fr.eni.bookhub.dal.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private CommentRepository commentRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    @Override
    public void addComment(int userId, int bookId, String text) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("Le commentaire ne peut pas être vide !");
        }

        final User user = validerUtilisateur(userId);
        final Book book = validerLivre(bookId);

        final Comment commentaire = new Comment();
        commentaire.setUser(user);
        commentaire.setBook(book);
        commentaire.setComment(text);
        commentaire.setCommentDate(LocalDateTime.now());
        commentaire.setReported(false);

        try {
            commentRepository.save(commentaire);
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'ajouter le commentaire");
        }
    }

    @Override
    public void signalerCommentaire(int commentId) {
        Comment commentaire = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire introuvable"));
        commentaire.setReported(true);
        commentRepository.save(commentaire);
    }

    @Override
    public List<Comment> getCommentairesSignales() {
        return commentRepository.findByReportedTrue();
    }

    @Override
    public void supprimerCommentaire(int commentId) {
        commentRepository.deleteById(commentId);
    }

    private User validerUtilisateur(int userId) {
        if (userId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas...");
        }
        final Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun utilisateur ne correspond à l'identifiant " + userId);
    }

    private Book validerLivre(int bookId) {
        if (bookId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas.");
        }
        final Optional<Book> opt = bookRepository.findById(bookId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun livre ne correspond à l'identifiant " + bookId);
    }
}
