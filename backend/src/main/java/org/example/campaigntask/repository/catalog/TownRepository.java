package org.example.campaigntask.repository.catalog;

import org.example.campaigntask.model.catalog.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TownRepository extends JpaRepository<Town, Long> {
}
