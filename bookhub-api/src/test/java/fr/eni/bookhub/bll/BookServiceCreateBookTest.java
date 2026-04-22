package fr.eni.bookhub.bll;

import static org.junit.jupiter.api.Assertions.*;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.dal.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class BookServiceCreateTest {

    @Autowired
    private BookServiceImpl bookService;

    @Autowired
    private BookRepository bookRepository;

    // Livre valide réutilisé dans plusieurs tests
    private Book book;

    @BeforeEach
    void create_data_book() {
        book = new Book();
        book.setTitle("Le Seigneur des Anneaux");
        book.setAuthor("J.R.R. Tolkien");
        book.setIsbn("978-0261102354");
        book.setCategory("Fantasy");
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
    }

    // Création du book + infos
    @Test
    void create_book() {
        Book saved = bookService.create(book);

        assertNotNull(saved);
        assertTrue(bookRepository.findById(saved.getId()).isPresent());
        assertEquals("Le Seigneur des Anneaux", saved.getTitle());
        assertEquals("J.R.R. Tolkien", saved.getAuthor());
    }

    // test sur date d'ajout
    @Test
    void create_book_added_date() {
        Book saved = bookService.create(book);
        assertNotNull(saved.getAddedAt());
    }

    // Livre null
    @Test
    void create_book_null() {
        assertThrows(RuntimeException.class, () -> bookService.create(null));
    }

    // Titre vide
    @Test
    void create_book_sans_title() {
        book.setTitle("");
        assertThrows(RuntimeException.class, () -> bookService.create(book));
    }

    // Auteur vide
    @Test
    void create_book_sans_author() {
        book.setAuthor("");
        assertThrows(RuntimeException.class, () -> bookService.create(book));
    }

    // ISBN null
    @Test
    void create_book_sans_isbn() {
        book.setIsbn(null);
        assertThrows(RuntimeException.class, () -> bookService.create(book));
    }

    // Nombre d'exemplaires nul ou négatif
    @Test
    void create_book_invalide_copies() {
        book.setTotalCopies(0);
        assertThrows(RuntimeException.class, () -> bookService.create(book));
    }
}
