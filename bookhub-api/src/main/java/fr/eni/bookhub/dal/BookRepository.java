package fr.eni.bookhub.dal;

import fr.eni.bookhub.bo.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Optional<Book> findByIsbn(String isbn);

    // retourne TRUE si au moins une ligne existe
    boolean existsByIsbn(String isbn);


    // Recherche par texte (titre, auteur, isbn), filtre catégorie et disponibilité
    // Chaque filtre est optionnel : si NULL, la condition est ignorée
    @Query("SELECT b FROM Book b WHERE "
            + "(:search IS NULL "
            +     "OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :search, '%')) "
            +     "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) "
            +     "OR LOWER(b.isbn)   LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "AND (:category IS NULL OR b.category = :category) "
            + "AND (:available IS NULL "
            +     "OR (:available = true  AND b.availableCopies > 0) "
            +     "OR (:available = false AND b.availableCopies = 0))")
    Page<Book> search(@Param("search") String search,
                      @Param("category") String category,
                      @Param("available") Boolean available,
                      Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findAllCategories();
}
