package org.example.campaigntask.repository.catalog;

import org.example.campaigntask.model.catalog.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(String name);
}
