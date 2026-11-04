package org.example.campaigntask.repository.seller;

import org.example.campaigntask.model.seller.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    Optional<Seller> findFirstByOrderByIdAsc();
}
