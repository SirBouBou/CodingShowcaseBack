package org.project.website.repository;

import org.project.website.dto.response.DBGameGenreProjection;
import org.project.website.dto.response.DBGamePlatformProjection;
import org.project.website.dto.response.DBGameResponse;
import org.project.website.model.DBGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DBGameRepository extends JpaRepository<DBGame, Long> {

    @Query("""
         SELECT new org.project.website.dto.response.DBGamePlatformProjection(
                  v.id,
                  p.name
              )
              FROM DBGame v
              JOIN v.platforms p
              WHERE v.id IN :gameIds
            """)
    Set<DBGamePlatformProjection> findPlatformsByGameIds(@Param("gameIds") List<Long> gameIds);

    @Query("""
         SELECT new org.project.website.dto.response.DBGameGenreProjection(
                  v.id,
                  g.name
              )
              FROM DBGame v
              JOIN v.genres g
              WHERE v.id IN :gameIds
            """)
    Set<DBGameGenreProjection> findGenresNyGameIds(@Param("gameIds") List<Long> gameIds);
    Page<DBGame> findAll(Pageable pageable);
}
