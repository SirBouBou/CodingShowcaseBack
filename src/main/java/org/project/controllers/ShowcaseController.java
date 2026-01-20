package org.project.controllers;

import org.project.models.Showcase;
import org.project.repository.ShowcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/showcase")
public class ShowcaseController {

    ShowcaseRepository showcaseRepository;

    @Autowired
    public ShowcaseController(ShowcaseRepository showcaseRepository) {
        this.showcaseRepository = showcaseRepository;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Showcase>> getAllShowcases() {
        List<Showcase> showcases = showcaseRepository.findAll();
        return ResponseEntity.ok().body(showcases);
    }
}
