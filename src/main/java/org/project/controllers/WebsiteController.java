package org.project.controllers;

import org.project.models.Website;
import org.project.services.WebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/website")
public class WebsiteController {
    WebsiteService websiteService;

    @Autowired
    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Website>> getAllWebsites() {
        List<Website> websites = websiteService.getAll();
        return ResponseEntity.ok().body(websites);
    }
}
