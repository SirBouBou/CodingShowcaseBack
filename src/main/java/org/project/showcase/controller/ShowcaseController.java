package org.project.showcase.controller;

import org.project.showcase.model.Showcase;
import org.project.showcase.service.ShowcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/showcase")
public class ShowcaseController {

    ShowcaseService showcaseService;

    @Autowired
    public ShowcaseController(ShowcaseService showcaseService) {
        this.showcaseService = showcaseService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Showcase>> getAllShowcases() {
        List<Showcase> showcases = showcaseService.getAll();
        return ResponseEntity.ok().body(showcases);
    }
}
