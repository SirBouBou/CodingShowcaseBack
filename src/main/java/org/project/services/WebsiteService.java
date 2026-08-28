package org.project.services;

import org.project.models.Website;
import org.project.repository.WebsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebsiteService {
    private final WebsiteRepository websiteRepository;

    @Autowired
    public WebsiteService(final WebsiteRepository websiteRepository) {
        this.websiteRepository = websiteRepository;
    }

    public List<Website> getAll() {
        return websiteRepository.findAll();
    }
}
