package org.project.game.controller;

import org.project.game.model.Game;
import org.project.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {

    GameService gameService;
    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAll();
        return ResponseEntity.ok().body(games);
    }
}
