package org.example.campaigntask.repository.catalog;

import org.example.campaigntask.model.catalog.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
