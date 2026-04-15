package fr.eni.bookhub.bo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_book")
    private int id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    // ISBN au maximum : 13 chiffres mais peut y avoir des tirets
    //L'ISBN est un numéro à 13 chiffres qui identifie chaque livre de manière unique
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    // Texte LONG (d'autres idées?)
    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    // Note moyenne calculée — ajouter une contrainte ou un CHK coté bdd si on veut rester < à 5 ou max 5.00
    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;
}
