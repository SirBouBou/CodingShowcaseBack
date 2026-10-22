package org.project.game.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.auth.service.CurrentIdentityService;
import org.project.game.dto.GameDto;
import org.project.game.dto.GameMoveRequest;
import org.project.game.dto.GameRoomDto;
import org.project.game.model.PlayerId;
import org.project.game.model.PlayerIdentity;
import org.project.game.model.PlayerType;
import org.project.game.service.GameRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameRoomControllerTest {
    private GameRoomService roomService;
    private CurrentIdentityService identityService;
    private GameRoomController controller;
    private Authentication authentication;
    private PlayerIdentity player;
    private GameRoomDto room;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        roomService = mock(GameRoomService.class);
        identityService = mock(CurrentIdentityService.class);
        controller = new GameRoomController(roomService, identityService);
        authentication = mock(Authentication.class);
        player = new PlayerIdentity(new PlayerId(PlayerType.USER, "1"), "player");
        roomId = UUID.randomUUID();
        room = mock(GameRoomDto.class);
        when(identityService.getCurrentIdentity(authentication)).thenReturn(player);
    }

    @Test
    void listsRoomsAndFiltersByGame() {
        when(roomService.getAll()).thenReturn(List.of(room));
        when(roomService.getRoomsForGame(2L)).thenReturn(List.of(room));

        assertEquals(List.of(room), controller.getAll().getBody());
        assertEquals(List.of(room), controller.getRoomForGame(2L).getBody());
        verify(roomService).getRoomsForGame(2L);
    }

    @Test
    void returnsFoundOrNotFoundForRoomAndGameLookups() {
        UUID missingRoomId = UUID.randomUUID();
        when(roomService.getRoom(roomId)).thenReturn(Optional.of(room));
        when(roomService.getRoom(missingRoomId)).thenReturn(Optional.empty());
        GameDto game = mock(GameDto.class);
        when(roomService.getGame(roomId)).thenReturn(Optional.of(game));
        when(roomService.getGame(missingRoomId)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.getRoom(roomId).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getRoom(missingRoomId).getStatusCode());
        assertSame(game, controller.getGame(roomId).getBody());
        assertEquals(HttpStatus.NOT_FOUND, controller.getGame(missingRoomId).getStatusCode());
    }

    @Test
    void resolvesCurrentRoomAndCreatesRoom() {
        when(roomService.getCurrentRoomForPlayer(player)).thenReturn(Optional.of(room));
        when(roomService.createGameRoom(2L, player)).thenReturn(Optional.of(room));
        when(roomService.createGameRoom(3L, player)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.OK, controller.getCurrentRoom(authentication).getStatusCode());
        assertEquals(HttpStatus.OK, controller.createRoom(2L, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.createRoom(3L, authentication).getStatusCode());

        when(roomService.getCurrentRoomForPlayer(player)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.getCurrentRoom(authentication).getStatusCode());
    }

    @Test
    void joinsAndLeavesRoom() {
        UUID rejectedRoomId = UUID.randomUUID();
        UUID unknownRoomId = UUID.randomUUID();
        when(roomService.joinRoom(roomId, player)).thenReturn(Optional.of(room));
        when(roomService.joinRoom(rejectedRoomId, player)).thenReturn(Optional.empty());
        when(roomService.leaveRoom(roomId, player)).thenReturn(true);
        when(roomService.leaveRoom(unknownRoomId, player)).thenReturn(false);

        assertEquals(HttpStatus.OK, controller.joinRoom(roomId, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.joinRoom(rejectedRoomId, authentication).getStatusCode());
        assertEquals(HttpStatus.OK, controller.leaveRoom(roomId, authentication).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.leaveRoom(unknownRoomId, authentication).getStatusCode());
    }

    @Test
    void playsRequestAndReturnsBadRequestWhenMoveIsRejected() {
        UUID rejectedRoomId = UUID.randomUUID();
        GameMoveRequest request = new GameMoveRequest();
        request.setAction("PLAY");
        request.setData(4);
        GameDto game = mock(GameDto.class);
        when(roomService.play(eq(roomId), eq(player), any())).thenReturn(Optional.of(game));
        when(roomService.play(eq(rejectedRoomId), eq(player), any())).thenReturn(Optional.empty());

        assertSame(game, controller.play(roomId, request, authentication).getBody());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.play(rejectedRoomId, request, authentication).getStatusCode());
    }
}
