package fr.eni.bookhub.bll;

import static org.junit.jupiter.api.Assertions.*;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.bo.Comment;
import fr.eni.bookhub.bo.Favorite;
import fr.eni.bookhub.bo.User;
import fr.eni.bookhub.dal.BookRepository;
import fr.eni.bookhub.dal.CommentRepository;
import fr.eni.bookhub.dal.FavoriteRepository;
import fr.eni.bookhub.dal.UserRepository;
import fr.eni.bookhub.dto.authentification.RegisterRequest;
import fr.eni.bookhub.dto.catalogue.BookDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
public class BookServiceDetailTest {

    @Autowired
    private BookServiceImpl bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    //tester si recup bien un commentaire
    @Autowired
    private CommentRepository commentRepository;

    //tester si favori works
    @Autowired
    private FavoriteRepository favoriteRepository;

    private int bookId;
    private int userId;

    // setup un user et un livre avant test
    @BeforeEach
    void create() {
        // user
        RegisterRequest userRequest = RegisterRequest.builder()
                .email("test@bookhub.fr")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .tosAcceptationDate(LocalDateTime.now())
                .build();
        userService.add(userRequest);

        User user = userRepository.findByEmail("test@bookhub.fr").orElseThrow();
        userId = user.getId();

        // book
        Book book = new Book();
        book.setTitle("1984");
        book.setAuthor("George Orwell");
        book.setIsbn("978-0451524935");
        book.setCategory("Dystopie");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        Book saved = bookService.create(book);
        bookId = saved.getId();
    }

    // getDetail renvoie objet avec le bon titre
    @Test
    void get_detail_ok() {
        BookDetailDTO detail = bookService.getDetail(bookId, userId);

        assertNotNull(detail);
        assertNotNull(detail.getBook());
        assertEquals("1984", detail.getBook().getTitle());
    }


    // Book pas en favori pour l'user test (comportement par défaut)
    @Test
    void get_detail_not_favorite_by_default() {
        BookDetailDTO detail = bookService.getDetail(bookId, userId);

        assertFalse(detail.isFavorite());
    }

    // Ajoute un fake comment et le récup
    @Test
    void get_detail_with_one_comment() {
        User user = userRepository.findById(userId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setBook(book);
        comment.setComment("Super livre !");
        comment.setReported(false);
        comment.setCommentDate(LocalDateTime.now());
        commentRepository.save(comment);

        BookDetailDTO detail = bookService.getDetail(bookId, userId);

        assertNotNull(detail.getComments());
        assertEquals(1, detail.getComments().size());
        assertEquals("Super livre !", detail.getComments().get(0).getComment());
    }

    // Ajoute un favori et voir si = à true
    @Test
    void get_detail_favorite_true_when_favorited() {
        favoriteRepository.save(new Favorite(userId, bookId));

        BookDetailDTO detail = bookService.getDetail(bookId, userId);

        assertTrue(detail.isFavorite());
    }

}
