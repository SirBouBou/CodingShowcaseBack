package org.project.game.controller;

import lombok.AllArgsConstructor;
import org.project.auth.service.CurrentIdentityService;
import org.project.game.dto.GameDto;
import org.project.game.dto.GameMoveRequest;
import org.project.game.dto.GameRoomDto;
import org.project.game.model.GameMove;
import org.project.game.model.PlayerIdentity;
import org.project.game.service.GameRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/game/rooms")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class GameRoomController {
    GameRoomService gameRoomService;
    CurrentIdentityService currentIdentityService;

    @GetMapping
    public ResponseEntity<List<GameRoomDto>> getAll() {
        List<GameRoomDto> result = gameRoomService.getAll();
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(params = "gameId")
    public ResponseEntity<List<GameRoomDto>> getRoomForGame(@RequestParam long gameId){
        List<GameRoomDto> result = gameRoomService.getRoomsForGame(gameId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<GameRoomDto> getRoom(@PathVariable UUID roomId) {
        Optional<GameRoomDto> result = gameRoomService.getRoom(roomId);
        if(result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(result.get());
    }

    @GetMapping("/current")
    public ResponseEntity<GameRoomDto> getCurrentRoom(Authentication authentication) {
        PlayerIdentity player = currentIdentityService.getCurrentIdentity(authentication);
        Optional<GameRoomDto> result = gameRoomService.getCurrentRoomForPlayer(player);
        if(result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(result.get());
    }

    @GetMapping("/{roomId}/game")
    public ResponseEntity<GameDto> getGame(@PathVariable UUID roomId) {
        return gameRoomService.getGame(roomId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(params = "gameId")
    public ResponseEntity<GameRoomDto> createRoom(@RequestParam long gameId, Authentication authentication) {
        PlayerIdentity player = currentIdentityService.getCurrentIdentity(authentication);
        Optional<GameRoomDto> result = gameRoomService.createGameRoom(gameId, player);
        if(result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(result.get());
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<GameRoomDto> joinRoom(@PathVariable UUID roomId, Authentication authentication) {
        PlayerIdentity player = currentIdentityService.getCurrentIdentity(authentication);
        Optional<GameRoomDto> result = gameRoomService.joinRoom(roomId, player);
        if(result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(result.get());
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity leaveRoom(@PathVariable UUID roomId, Authentication authentication) {
        PlayerIdentity player = currentIdentityService.getCurrentIdentity(authentication);
        boolean result = gameRoomService.leaveRoom(roomId, player);
        if(!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/play")
    public ResponseEntity<GameDto> play(@PathVariable UUID roomId, @RequestBody GameMoveRequest request, Authentication authentication) {
        PlayerIdentity player = currentIdentityService.getCurrentIdentity(authentication);
        GameMove move = new GameMove(
                player,
                request.getAction(),
                request.getData()
        );
        Optional<GameDto> result =
                gameRoomService.play(roomId, player, move);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(result.get());
    }
}
