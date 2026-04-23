package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByBookId(Integer bookId);

    List<Comment> findByUserId(Integer userId);

    List<Comment> findByReportedTrue();

}
