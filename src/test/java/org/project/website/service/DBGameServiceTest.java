package org.project.website.service;

import org.junit.jupiter.api.Test;
import org.project.website.dto.response.DBGameGenreProjection;
import org.project.website.dto.response.DBGamePlatformProjection;
import org.project.website.dto.response.DBGameResponse;
import org.project.website.model.DBGame;
import org.project.website.repository.DBGameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DBGameServiceTest {
    @Test
    void mapsPagedGamesAndGroupsTheirPlatformsAndGenres() {
        DBGame first = game(1L, "First");
        DBGame second = game(2L, "Second");
        DBGameRepository repository = mock(DBGameRepository.class);
        when(repository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 10), 2));
        when(repository.findPlatformsByGameIds(List.of(1L, 2L))).thenReturn(Set.of(
                new DBGamePlatformProjection(1L, "PC"),
                new DBGamePlatformProjection(1L, "Console"),
                new DBGamePlatformProjection(2L, "PC")
        ));
        when(repository.findGenresNyGameIds(List.of(1L, 2L))).thenReturn(Set.of(
                new DBGameGenreProjection(1L, "Action"),
                new DBGameGenreProjection(2L, "Puzzle")
        ));

        Page<DBGameResponse> response =
                new DBGameService(repository).getGames(PageRequest.of(0, 10));

        assertEquals(2, response.getTotalElements());
        assertEquals(Set.of("PC", "Console"), response.getContent().get(0).platforms());
        assertEquals(Set.of("Action"), response.getContent().get(0).genres());
        assertEquals(Set.of("PC"), response.getContent().get(1).platforms());
        assertEquals(Set.of("Puzzle"), response.getContent().get(1).genres());
        assertEquals(LocalDate.of(2024, 1, 2), response.getContent().get(0).released());
        verify(repository).findPlatformsByGameIds(List.of(1L, 2L));
        verify(repository).findGenresNyGameIds(List.of(1L, 2L));
    }

    @Test
    void mapsEmptyPageWithoutQueryingRelatedData() {
        DBGameRepository repository = mock(DBGameRepository.class);
        PageRequest pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(Page.empty(pageable));
        when(repository.findPlatformsByGameIds(List.of())).thenReturn(Set.of());
        when(repository.findGenresNyGameIds(List.of())).thenReturn(Set.of());

        Page<DBGameResponse> response = new DBGameService(repository).getGames(pageable);

        assertTrue(response.isEmpty());
        verify(repository).findPlatformsByGameIds(List.of());
        verify(repository).findGenresNyGameIds(List.of());
    }

    @Test
    void usesEmptyCollectionsWhenGameHasNoProjectionRows() {
        DBGameRepository repository = mock(DBGameRepository.class);
        PageRequest pageable = PageRequest.of(0, 1);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(game(3L, "Third"))));
        when(repository.findPlatformsByGameIds(List.of(3L))).thenReturn(Set.of());
        when(repository.findGenresNyGameIds(List.of(3L))).thenReturn(Set.of());

        DBGameResponse response = new DBGameService(repository).getGames(pageable).getContent().get(0);

        assertTrue(response.platforms().isEmpty());
        assertTrue(response.genres().isEmpty());
    }

    private static DBGame game(Long id, String name) {
        DBGame game = new DBGame();
        game.setId(id);
        game.setName(name);
        game.setReleased(LocalDate.of(2024, 1, 2));
        game.setRating(4.5f);
        game.setRatingCount(12);
        game.setMetacritics(85);
        game.setImage_url("cover.png");
        return game;
    }
}
