package org.project.website.service;

import org.project.website.dto.response.DBGameGenreProjection;
import org.project.website.dto.response.DBGamePlatformProjection;
import org.project.website.dto.response.DBGameResponse;
import org.project.website.model.DBGame;
import org.project.website.repository.DBGameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class DBGameService {
    private final DBGameRepository dbGameRepository;

    public DBGameService(DBGameRepository dbGameRepository) {
        this.dbGameRepository = dbGameRepository;
    }

    public Page<DBGameResponse> getGames(Pageable pageable) {
        Page<DBGame> games = dbGameRepository.findAll(pageable);
        List<Long> gameIds = games.getContent()
                .stream()
                .map(DBGame::getId)
                .toList();

        Set<DBGamePlatformProjection> platforms =
                dbGameRepository.findPlatformsByGameIds(gameIds);

        Map<Long, Set<String>> platformsByGame =
                platforms.stream()
                        .collect(Collectors.groupingBy(
                                DBGamePlatformProjection::gameId,
                                Collectors.mapping(
                                        DBGamePlatformProjection::platformName,
                                        Collectors.toSet()
                                )
                        ));

        Set<DBGameGenreProjection> genres =
                dbGameRepository.findGenresNyGameIds(gameIds);

        Map<Long, Set<String>> genresByGame =
                genres.stream()
                        .collect(Collectors.groupingBy(
                                DBGameGenreProjection::gameId,
                                Collectors.mapping(
                                        DBGameGenreProjection::genreName,
                                        Collectors.toSet()
                                )
                        ));

        return games.map(game ->
                new DBGameResponse(
                        game.getId(),
                        game.getName(),
                        game.getReleased(),
                        game.getRating(),
                        game.getRating_count(),
                        game.getMetacritics(),
                        game.getImage_url(),
                        platformsByGame.getOrDefault(
                                game.getId(),
                                Set.of()
                        ),
                        genresByGame.getOrDefault(
                                game.getId(),
                                Set.of()
                        )
                ));
    }
}
