package org.project.showcase.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.showcase.model.Showcase;
import org.project.showcase.repository.ShowcaseRepository;
import org.project.showcase.service.ShowcaseService;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)

public class ShowcaseServiceTest {
    private ShowcaseService showcaseService;
    @Mock
    private ShowcaseRepository showcaseRepository;

    @BeforeEach
    public void setUp() {
        showcaseService = new ShowcaseService(showcaseRepository);
        Showcase show1 = new Showcase("show1", "descShow1");
        Showcase show2 = new Showcase("show2", "descShow2");
        List<Showcase> shows = Arrays.asList(show1, show2);

        Mockito.when(showcaseRepository.findAll())
                .thenReturn(shows);
    }

    @Test
    public void shouldGetAllShowcases() {
        List<Showcase> found = showcaseService.getAll();
        assertEquals("Le nombre de showcase trouvés devrait être 2", 2, found.size());
    }
}
