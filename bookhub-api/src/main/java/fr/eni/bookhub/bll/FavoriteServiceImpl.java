package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Favorite;
import fr.eni.bookhub.bo.FavoriteId;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.FavoriteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private FavoriteRepository favoriteRepository;
    private BookRepository bookRepository;

    @Override
    public List<Book> getFavorites(int userId) {
        if (userId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas...");
        }
        final List<Favorite> favoris = favoriteRepository.findByUserId(userId);
        final List<Book> books = new ArrayList<>();
        for (Favorite f : favoris) {
            final Optional<Book> opt = bookRepository.findById(f.getBookId());
            if (opt.isPresent()) {
                books.add(opt.get());
            }
        }
        return books;
    }

    @Override
    public void addFavorite(int userId, int bookId) {
        validerLivre(bookId);
        // Ne pas ajouter en double
        if (favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            return;
        }
        try {
            favoriteRepository.save(new Favorite(userId, bookId));
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'ajouter le favori ! ");
        }
    }

    @Override
    public void removeFavorite(int userId, int bookId) {
        if (userId <= 0 || bookId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas...");
        }
        try {
            favoriteRepository.deleteById(new FavoriteId(userId, bookId));
        } catch (Exception e) {
            throw new RuntimeException("Impossible de supprimer le favori ! ");
        }
    }

    @Override
    public boolean isFavorite(int userId, int bookId) {
        return favoriteRepository.existsByUserIdAndBookId(userId, bookId);
    }

    private Book validerLivre(int bookId) {
        if (bookId <= 0) {
            throw new RuntimeException("Identifiant n'existe pas...");
        }
        final Optional<Book> opt = bookRepository.findById(bookId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new RuntimeException("Aucun livre ne correspond à l'identifiant " + bookId);
    }
}
