package org.project.website.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="game_genre")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DBGameGenre {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long id_rawg;
    private String name;
    private Integer game_count;

}
