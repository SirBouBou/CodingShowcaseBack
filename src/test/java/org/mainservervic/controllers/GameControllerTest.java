package org.mainservervic.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mainservervic.models.Game;
import org.mainservervic.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {
    @Mock
    GameRepository gameRepository;

    @InjectMocks
    GameController gameController;

    @Test
    void getAllGames() {
       Game game1 = new Game(1, "test1", "desc1");
       Game game2 = new Game(2, "test2", "desc2");
       List<Game> games = List.of(game1, game2);
       when(gameRepository.findAll()).thenReturn(games);

       ResponseEntity<List<Game>> result = gameController.getAllGames();

       assertEquals(HttpStatus.OK, result.getStatusCode());
       assertNotNull(result.getBody());
       assertEquals(2, result.getBody().size());
    }
}