package fr.eni.bookhub.bll;

import static org.junit.jupiter.api.Assertions.*;

import fr.eni.bookhub.bo.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class BookServiceSearchBookTest {

    @Autowired
    private BookServiceImpl bookService;

    // Insère un livre avant chaque teste
    @BeforeEach
    void setup() {
        Book book = new Book();
        book.setTitle("Harry Potter");
        book.setAuthor("J.K. Rowling");
        book.setIsbn("978-0439708180");
        book.setCategory("Fantasy");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        bookService.create(book);
    }

    // Recherche par titre
    @Test
    void search_by_title() {
        Page<Book> result = bookService.search("Harry Potter", null, null, PageRequest.of(0, 20));

        assertTrue(result.getTotalElements() >= 1);
        assertEquals("Harry Potter", result.getContent().get(0).getTitle());
    }

    // Recherche par auteur
    @Test
    void search_by_author() {
        Page<Book> result = bookService.search("Rowling", null, null, PageRequest.of(0, 20));

        assertTrue(result.getTotalElements() >= 1);
    }

    // Recherche sans résultat 
    @Test
    void search_with_fake_data() {
        Page<Book> result = bookService.search("rien", null, null, PageRequest.of(0, 20));

        assertEquals(0, result.getTotalElements());
    }

    // Filtre catégorie / livres par catégories
    @Test
    void search_by_category() {
        Page<Book> result = bookService.search(null, "Fantasy", null, PageRequest.of(0, 20));

        assertTrue(result.getTotalElements() >= 1);
        result.getContent().forEach(b -> assertEquals("Fantasy", b.getCategory()));
    }


    // getCategories : liste  contient "Fantasy"
    @Test
    void get_categories_contains_fantasy() {
        List<String> categories = bookService.getCategories();

        assertNotNull(categories);
        assertTrue(categories.contains("Fantasy"));
    }
}
