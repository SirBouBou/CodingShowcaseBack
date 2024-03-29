package org.project.controllers;

import org.project.models.Game;
import org.project.payload.response.UserInfoResponse;
import org.project.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    GameRepository gameRepository;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllGames() {
        List<Game> games = gameRepository.findAll();
        return ResponseEntity.ok().body(games);
    }
}
