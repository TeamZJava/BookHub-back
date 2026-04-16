package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    List<Rating> findByBookId(Integer bookId);

    Optional<Rating> findByUserIdAndBookId(Integer userId, Integer bookId);
}
