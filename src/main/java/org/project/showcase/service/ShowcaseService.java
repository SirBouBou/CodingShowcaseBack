package org.project.showcase.service;

import org.project.showcase.model.Showcase;
import org.project.showcase.repository.ShowcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowcaseService {

    private final ShowcaseRepository showcaseRepository;

    @Autowired
    public ShowcaseService(final ShowcaseRepository showcaseRepository) {
        this.showcaseRepository = showcaseRepository;
    }

    public List<Showcase> getAll() {
        return showcaseRepository.findAll();
    }
}
