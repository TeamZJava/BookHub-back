package fr.eni.bookhub.bll;

public interface CommentService {

    void addComment(int userId, int bookId, String text);
}
