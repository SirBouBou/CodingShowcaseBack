package org.project.website.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteAndGameEntitiesTest {
    @Test
    void websiteSupportsConstructorsAndPropertyUpdates() {
        Website website = new Website("Arcade", "Game showcase");
        website.setId(3);

        assertEquals(3, website.getId());
        assertEquals("Arcade", website.getName());
        assertEquals("Game showcase", website.getDescription());

        Website empty = new Website();
        empty.setId(4);
        empty.setName("Portal");
        empty.setDescription("Description");
        assertEquals(4, empty.getId());
        assertEquals("Portal", empty.getName());
        assertEquals("Description", empty.getDescription());

        Website complete = new Website(5, "Catalog", "All games");
        assertEquals(5, complete.getId());
        assertEquals("Catalog", complete.getName());
        assertEquals("All games", complete.getDescription());
    }

    @Test
    void genreAndPlatformExposeAllPersistentFields() {
        DBGameGenre genre = new DBGameGenre(1L, 11L, "Puzzle", 50);
        assertEquals(1L, genre.getId());
        assertEquals(11L, genre.getId_rawg());
        assertEquals("Puzzle", genre.getName());
        assertEquals(50, genre.getGame_count());
        genre.setId(2L);
        genre.setId_rawg(22L);
        genre.setName("Action");
        genre.setGame_count(60);
        assertEquals(2L, genre.getId());
        assertEquals(22L, genre.getId_rawg());
        assertEquals("Action", genre.getName());
        assertEquals(60, genre.getGame_count());

        DBGameGenre emptyGenre = new DBGameGenre();
        emptyGenre.setName("Adventure");
        assertEquals("Adventure", emptyGenre.getName());

        DBGamePlatform platform = new DBGamePlatform(3L, 33L, "PC", 100);
        assertEquals(3L, platform.getId());
        assertEquals(33L, platform.getId_rawg());
        assertEquals("PC", platform.getName());
        assertEquals(100, platform.getGame_count());
        platform.setId(4L);
        platform.setId_rawg(44L);
        platform.setName("Console");
        platform.setGame_count(200);
        assertEquals(4L, platform.getId());
        assertEquals(44L, platform.getId_rawg());
        assertEquals("Console", platform.getName());
        assertEquals(200, platform.getGame_count());

        DBGamePlatform emptyPlatform = new DBGamePlatform();
        emptyPlatform.setName("Mobile");
        assertEquals("Mobile", emptyPlatform.getName());
    }

    @Test
    void databaseGamePreservesAllFieldsAcrossConstructorsAndAccessors() {
        DBGamePlatform platform = new DBGamePlatform(1L, 101L, "PC", 20);
        DBGameGenre genre = new DBGameGenre(2L, 202L, "RPG", 30);
        Set<DBGamePlatform> platforms = Set.of(platform);
        Set<DBGameGenre> genres = Set.of(genre);
        DBGame game = new DBGame(
                7L, 70L, "Roleplay", LocalDate.of(2020, 1, 1),
                4.2f, 200, 88, "cover.png", platforms, genres
        );

        assertEquals(7L, game.getId());
        assertEquals(70L, game.getRawg_id());
        assertEquals("Roleplay", game.getName());
        assertEquals(LocalDate.of(2020, 1, 1), game.getReleased());
        assertEquals(4.2f, game.getRating());
        assertEquals(200, game.getRatingCount());
        assertEquals(88, game.getMetacritics());
        assertEquals("cover.png", game.getImage_url());
        assertEquals(platforms, game.getPlatforms());
        assertEquals(genres, game.getGenres());

        game.setId(8L);
        game.setRawg_id(80L);
        game.setName("Updated");
        game.setReleased(null);
        game.setRating(3.5f);
        game.setRatingCount(100);
        game.setMetacritics(75);
        game.setImage_url("updated.png");
        game.setPlatforms(Set.of());
        game.setGenres(Set.of());
        assertEquals(8L, game.getId());
        assertEquals(80L, game.getRawg_id());
        assertEquals("Updated", game.getName());
        assertNull(game.getReleased());
        assertEquals(3.5f, game.getRating());
        assertEquals(100, game.getRatingCount());
        assertEquals(75, game.getMetacritics());
        assertEquals("updated.png", game.getImage_url());
        assertTrue(game.getPlatforms().isEmpty());
        assertTrue(game.getGenres().isEmpty());

        assertNotNull(new DBGame());
    }
}
