package org.project.website.dto.response;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

public record DBGameResponse(
        Long id,
        String name,
        LocalDate released,
        Float rating,
        Integer ratingCount,
        Integer metacritics,
        String imageUrl,
        Set<String> platforms,
        Set<String> genres
)
{}
