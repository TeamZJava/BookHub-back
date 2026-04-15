package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Optional<Book> findByIsbn(String isbn);

    // retourne TRUE si au moins une ligne existe
    boolean existsByIsbn(String isbn);
}
