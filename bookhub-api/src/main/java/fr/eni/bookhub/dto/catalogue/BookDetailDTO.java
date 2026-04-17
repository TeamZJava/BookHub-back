package fr.eni.bookhub.dto.catalogue;

import fr.eni.bookhub.bo.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailDTO {

    private Book book;
    private List<CommentDTO> comments;
    private boolean favorite;
    private Integer userRating;
}
