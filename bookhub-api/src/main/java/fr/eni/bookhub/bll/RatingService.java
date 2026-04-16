package fr.eni.bookhub.bll;

public interface RatingService {

    void rateBook(int userId, int bookId, int note);
}
