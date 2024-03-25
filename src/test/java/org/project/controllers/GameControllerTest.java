package org.project.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.models.Game;
import org.project.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {
    @Mock
    GameRepository gameRepository;

    @InjectMocks
    GameController gameController;

    @Test
    void getAllGames() throws Exception {
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