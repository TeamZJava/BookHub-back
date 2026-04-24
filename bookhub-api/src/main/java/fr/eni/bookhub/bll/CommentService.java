package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Comment;

import java.util.List;

public interface CommentService {

    void addComment(int userId, int bookId, String text);

    void signalerCommentaire(int commentId);

    List<Comment> getCommentairesSignales();

    void supprimerCommentaire(int commentId);
}
