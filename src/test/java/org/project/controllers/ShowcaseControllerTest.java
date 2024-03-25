package org.project.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.models.Showcase;
import org.project.repository.GameRepository;
import org.project.repository.ShowcaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowcaseControllerTest {
    @Mock
    ShowcaseRepository showcaseRepository;

    @InjectMocks
    ShowcaseController showcaseController;

    @Test
    void getAllShowcases() {
        Showcase showcase1 = new Showcase(1, "test1", "Desc1");
        Showcase showcase2 = new Showcase(2, "test2", "Desc2");
        List<Showcase> showcases = List.of(showcase1, showcase2);
        when(showcaseRepository.findAll()).thenReturn(showcases);

        ResponseEntity<List<Showcase>> result = showcaseController.getAllShowcases();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
    }
}