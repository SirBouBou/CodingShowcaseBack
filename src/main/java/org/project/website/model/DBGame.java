package org.project.website.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name="videogames")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DBGame {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rawg_id;

    @NotBlank
    @Size(max = 255)
    private String name;

    private LocalDate released;

    private Float rating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    private Integer metacritics;

    private String image_url;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "videogames_platforms",
            joinColumns = @JoinColumn(name = "id_videogame"),
            inverseJoinColumns = @JoinColumn(name = "id_platforms"))
    private Set<DBGamePlatform> platforms;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "videogame_game_genre",
            joinColumns = @JoinColumn(name = "id_video_game"),
            inverseJoinColumns = @JoinColumn(name = "id_game_genre"))
    private Set<DBGameGenre> genres;
}
