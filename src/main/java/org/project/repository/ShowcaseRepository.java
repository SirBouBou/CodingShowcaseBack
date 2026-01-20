package org.project.repository;

import org.project.models.Showcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowcaseRepository extends JpaRepository<Showcase, Long> {
}
