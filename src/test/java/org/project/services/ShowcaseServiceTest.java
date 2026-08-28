package org.project.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.models.Showcase;
import org.project.repository.ShowcaseRepository;

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
