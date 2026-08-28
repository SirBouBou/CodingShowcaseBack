package org.project.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.game.model.Game;
import org.project.game.service.GameService;
import org.project.game.repository.GameRepository;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    private GameService gameService;
    @Mock
    private GameRepository gameRepository;

    @BeforeEach
    public void setUp() {
        gameService = new GameService(gameRepository);
        Game game1 = new Game("game1", "descGame1");
        Game game2 = new Game("game2", "descGame2");
        List<Game> games = Arrays.asList(game1, game2);

        Mockito.when(gameRepository.findAll())
                .thenReturn(games);
    }

    @Test
    public void shouldGetAllGames() {
        List<Game> found = gameService.getAll();
        assertEquals("Le nombre de jeux trouvés devrait être 2", 2, found.size());
    }
}
