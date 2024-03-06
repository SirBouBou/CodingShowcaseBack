package org.project.controllers;

import org.project.models.Game;
import org.project.models.Showcase;
import org.project.repository.ShowcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/showcase")
public class ShowcaseController {

    @Autowired
    ShowcaseRepository showcaseRepository;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllShowcases() {
        List<Showcase> showcases = showcaseRepository.findAll();
        return ResponseEntity.ok().body(showcases);
    }
}
