package fr.eni.bookhub.bo;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteId implements Serializable {

    private static final long serialVersionUID = 1L;

    // pas besoin de Column car pas une entité
    private int userId;
    private int bookId;
}
