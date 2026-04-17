package fr.eni.bookhub.bo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class Favorite {

    @Id
    @Column(name = "id_user")
    private int userId;

    @Id
    @Column(name = "id_book")
    private int bookId;
}
