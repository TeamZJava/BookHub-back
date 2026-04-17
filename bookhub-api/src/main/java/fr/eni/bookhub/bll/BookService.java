package fr.eni.bookhub.bll;

import fr.eni.bookhub.bo.Book;
import fr.eni.bookhub.controller.dto.BookDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    Page<Book> search(String search, String category, Boolean available, Pageable pageable);

    BookDetailDTO getDetail(int bookId, int userId);

    Book create(Book book);

    Book update(int id, Book book);

    void delete(int id);

    List<String> getCategories();
}