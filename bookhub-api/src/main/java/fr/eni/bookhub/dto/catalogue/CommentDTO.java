package fr.eni.bookhub.dto.catalogue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private int id;
    private String firstName;
    private String lastName;
    private String comment;
    private LocalDateTime commentDate;
    private Integer userRating;
    private Boolean reported;
}
