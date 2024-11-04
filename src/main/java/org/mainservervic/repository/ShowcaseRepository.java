package org.mainservervic.repository;

import org.mainservervic.models.Showcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowcaseRepository extends JpaRepository<Showcase, Long> {
}
