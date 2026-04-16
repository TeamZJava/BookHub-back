package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Favorite;
import fr.eni.bookhub.bo.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findByUserId(int userId);

    boolean existsByUserIdAndBookId(int userId, int bookId);
}
