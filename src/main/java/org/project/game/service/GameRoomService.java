package org.project.game.service;

import lombok.RequiredArgsConstructor;
import org.project.game.dto.GameDto;
import org.project.game.dto.GameEvent;
import org.project.game.dto.GameRoomDto;
import org.project.game.dto.RoomEvent;
import org.project.game.model.*;
import org.project.game.ticatactoe.TicTacToeGame;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final Map<UUID, GameRoom> rooms = new HashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public List<GameRoomDto> getAll() {
        List<GameRoomDto> result = new ArrayList<>();
        rooms.forEach((k,v) -> result.add(new GameRoomDto(v)));
        return result;
    }

    public List<GameRoomDto> getRoomsForGame(long gameId) {
        List<GameRoomDto> result = new ArrayList<>();
        rooms.forEach((k,v) -> {
            if(v.getGameId() == gameId) {
                result.add(new GameRoomDto(v));
            }
        });
        return result;
    }

    public Optional<GameRoomDto> getRoom(UUID roomId) {
        Optional<GameRoom> result = Optional.ofNullable(rooms.get(roomId));
        return result.map(GameRoomDto::new);
    }

    public Optional<GameRoomDto> getCurrentRoomForPlayer(PlayerIdentity player) {
        for (GameRoom room : rooms.values()) {
            for(PlayerIdentity roomPlayer : room.getPlayers()) {
                if(player.equals(roomPlayer)) {
                    return Optional.of(new GameRoomDto(room));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<GameRoomDto> createGameRoom(long gameId, PlayerIdentity player) {
        if (getCurrentRoomForPlayer(player).isPresent()) {
            return Optional.empty();
        }
        return switch((int) gameId) {
            case 2 -> Optional.of(createTicTacToe(player));
            default -> Optional.empty();
        };
    }

    public GameRoomDto createTicTacToe(PlayerIdentity player) {
        TicTacToeGame game = new TicTacToeGame(player);
        UUID uuid = UUID.randomUUID();
        GameRoom room = new GameRoom(uuid, 2L, 2, 1, new PlayerIdentity[]{player, null}, game);
        rooms.put(uuid, room);
        GameRoomDto dto = new GameRoomDto(room);
        messagingTemplate.convertAndSend(
                "/topic/rooms",
                new RoomEvent(RoomEventType.CREATED, dto)
        );
        return dto;
    }

    public Optional<GameRoomDto> joinRoom(UUID roomId, PlayerIdentity player) {
        GameRoom room = rooms.get(roomId);
        if(room == null) {
            return Optional.empty();
        }
        Optional<GameRoomDto> currentRoom = getCurrentRoomForPlayer(player);

        if (currentRoom.isPresent()) {
            return Optional.empty();
        }

        GameStatus previousStatus = room.getGame().getStatus();

        if (!room.addPlayer(player)) {
            return Optional.empty();
        }

        GameRoomDto dto = new GameRoomDto(room);
        messagingTemplate.convertAndSend(
                "/topic/rooms",
                new RoomEvent(RoomEventType.JOINED, dto)
        );

        GameStatus newStatus = room.getGame().getStatus();

        if (previousStatus != GameStatus.PLAYING
                && newStatus == GameStatus.PLAYING) {

            messagingTemplate.convertAndSend(
                    "/topic/rooms/" + roomId,
                    new GameEvent(
                            GameEventType.GAME_STARTED,
                            room.getGameDto()
                    )
            );
        }
        return Optional.of(dto);
    }

    public boolean leaveRoom(UUID roomId, PlayerIdentity player) {
        GameRoom room = rooms.get(roomId);
        if(room == null || !room.removePlayer(player)) {
            return false;
        }
        GameRoomDto dto = new GameRoomDto(room);

        messagingTemplate.convertAndSend(
                "/topic/rooms",
                new RoomEvent(RoomEventType.LEFT, dto)
        );
        if(room.getActivePlayers() == 0) {
            rooms.remove(roomId);
        }
        return true;
    }

    public Optional<GameDto> getGame(UUID roomId) {
        Optional<GameDto> result = Optional.ofNullable(rooms.get(roomId).getGameDto());
        return result;
    }

    public Optional<GameDto> play(UUID roomId, PlayerIdentity player, GameMove move) {
        GameRoom room = rooms.get(roomId);

        if (room == null) {
            return Optional.empty();
        }

        if (!room.play(player, move)) {
            return Optional.empty();
        }

        GameDto gameDto = room.getGameDto();

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId,
                new GameEvent(GameEventType.MOVE_PLAYED, gameDto)
        );

        return Optional.of(gameDto);
    }

}
