package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;

import java.util.List;

public interface FavoriteService {

    List<Book> getFavorites(int userId);

    void addFavorite(int userId, int bookId);

    void removeFavorite(int userId, int bookId);

    boolean isFavorite(int userId, int bookId);
}
