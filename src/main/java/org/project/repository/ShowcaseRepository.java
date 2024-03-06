package org.project.repository;

import org.project.models.Role;
import org.project.models.Showcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowcaseRepository extends JpaRepository<Showcase, Long> {
}
