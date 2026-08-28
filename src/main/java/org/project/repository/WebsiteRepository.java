package org.project.repository;

import org.project.models.Showcase;
import org.project.models.Website;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {
}
