package org.project.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.game.dto.GameDto;
import org.project.game.dto.GameEvent;
import org.project.game.dto.GameRoomDto;
import org.project.game.dto.RoomEvent;
import org.project.game.dto.TicTacToeGameDto;
import org.project.game.model.*;
import org.project.game.ticatactoe.TicTacToeCell;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameRoomServiceTest {
    private SimpMessagingTemplate messagingTemplate;
    private GameRoomService service;
    private final PlayerIdentity creator = player("creator");
    private final PlayerIdentity guest = player("guest");

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        service = new GameRoomService(messagingTemplate);
    }

    @Test
    void createsAndListsTicTacToeRoom() {
        GameRoomDto room = service.createGameRoom(2, creator).orElseThrow();

        assertEquals(2, room.getGameId());
        assertEquals(1, room.getActivePlayers());
        assertEquals(creator, room.getPlayers()[0]);
        assertNull(room.getPlayers()[1]);
        assertEquals(1, service.getAll().size());
        assertEquals(1, service.getRoomsForGame(2).size());
        assertTrue(service.getRoomsForGame(1).isEmpty());
        assertEquals(room.getId(), service.getRoom(room.getId()).orElseThrow().getId());
        assertEquals(room.getId(), service.getCurrentRoomForPlayer(creator).orElseThrow().getId());
        verify(messagingTemplate).convertAndSend(eq("/topic/rooms"), any(RoomEvent.class));
    }

    @Test
    void rejectsUnsupportedGameAndPlayersAlreadyInRoom() {
        assertTrue(service.createGameRoom(999, creator).isEmpty());
        GameRoomDto room = service.createTicTacToe(creator);

        assertTrue(service.createGameRoom(2, creator).isEmpty());
        assertTrue(service.joinRoom(room.getId(), creator).isEmpty());
        assertTrue(service.getRoom(UUID.randomUUID()).isEmpty());
        assertTrue(service.getCurrentRoomForPlayer(guest).isEmpty());
    }

    @Test
    void joinsStartsAndPublishesGame() {
        GameRoomDto room = service.createTicTacToe(creator);

        GameRoomDto joined = service.joinRoom(room.getId(), guest).orElseThrow();

        assertEquals(2, joined.getActivePlayers());
        assertEquals(guest, joined.getPlayers()[1]);
        assertEquals(GameStatus.PLAYING, service.getGame(room.getId())
                .map(game -> ((TicTacToeGameDto) game).getStatus()).orElseThrow());
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/rooms"), any(RoomEvent.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/rooms/" + room.getId()), any(GameEvent.class));
        assertTrue(service.joinRoom(UUID.randomUUID(), guest).isEmpty());
    }

    @Test
    void playsValidMoveAndRejectsInvalidMove() {
        GameRoomDto room = service.createTicTacToe(creator);
        service.joinRoom(room.getId(), guest).orElseThrow();

        assertTrue(service.play(room.getId(), player("outsider"),
                new GameMove(player("outsider"), "PLAY", 0)).isEmpty());

        TicTacToeGameDto game = (TicTacToeGameDto) service.getGame(room.getId()).orElseThrow();
        PlayerIdentity currentPlayer = game.getCurrentPlayer();
        GameDto result = service.play(room.getId(), currentPlayer,
                new GameMove(currentPlayer, "PLAY", 0)).orElseThrow();

        TicTacToeCell expected = currentPlayer.equals(game.getPlayerX())
                ? TicTacToeCell.X
                : TicTacToeCell.O;
        assertEquals(expected, ((TicTacToeGameDto) result).getBoard()[0]);
        assertTrue(service.play(UUID.randomUUID(), creator,
                new GameMove(creator, "PLAY", 1)).isEmpty());
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/rooms/" + room.getId()), any(GameEvent.class));
    }

    @Test
    void leavesRoomAndRemovesItAfterLastPlayerLeaves() {
        GameRoomDto room = service.createTicTacToe(creator);

        assertFalse(service.leaveRoom(UUID.randomUUID(), creator));
        assertFalse(service.leaveRoom(room.getId(), guest));
        assertTrue(service.leaveRoom(room.getId(), creator));
        assertTrue(service.getRoom(room.getId()).isEmpty());
        assertTrue(service.getAll().isEmpty());
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/rooms"), any(RoomEvent.class));
    }

    @Test
    void rejectsLeavingAndRejoiningSameIdentityDuringOneRoomLifecycle() {
        GameRoomDto room = service.createTicTacToe(creator);
        service.joinRoom(room.getId(), guest).orElseThrow();

        assertTrue(service.leaveRoom(room.getId(), guest));
        assertTrue(service.getRoom(room.getId()).isPresent());
        assertEquals(1, service.getRoom(room.getId()).orElseThrow().getActivePlayers());
        assertTrue(service.joinRoom(room.getId(), creator).isEmpty());
        assertTrue(service.getGame(UUID.randomUUID()).isEmpty());
        assertTrue(service.getGame(room.getId()).isPresent());
    }

    private static PlayerIdentity player(String id) {
        return new PlayerIdentity(new PlayerId(PlayerType.USER, id), id);
    }
}
