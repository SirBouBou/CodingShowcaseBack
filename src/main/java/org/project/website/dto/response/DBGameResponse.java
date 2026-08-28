package org.project.website.dto.response;

import java.util.Date;
import java.util.Set;

public record DBGameResponse(
        Long id,
        String name,
        Date released,
        Float rating,
        Integer rating_count,
        Integer metacritics,
        String image_url,
        Set<String> platforms,
        Set<String> genres
)
{}
