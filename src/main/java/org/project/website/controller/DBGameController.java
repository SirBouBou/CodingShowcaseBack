package org.project.website.controller;

import org.project.website.dto.response.DBGameResponse;
import org.project.website.service.DBGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/website/dbgame")
public class DBGameController {
    DBGameService dbGameService;

    @Autowired
    public DBGameController(DBGameService dbGameService) {
        this.dbGameService = dbGameService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<DBGameResponse>> getAll(Pageable pageable) {
        Page<DBGameResponse> result = dbGameService.getGames(pageable);
        return ResponseEntity.ok().body(result);
    }
}
