package fr.eni.bookhub.bo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comment")
    private int id;

    // FK -> users
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // FK -> books
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_book", nullable = false)
    private Book book;

    // Texte du comm 1000 caractères max
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String comment;

    // Commentaire signalé par un autre utilisateur
    @Column(nullable = false)
    private Boolean reported = false;
    // comment on fait, on récup les commentaires signalés avec
    // List<Comment> findByBadComment()?

    @Column(name = "comment_date", nullable = false)
    private LocalDateTime commentDate;
}
