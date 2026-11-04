package org.example.campaigntask.service.seller;

import org.example.campaigntask.dto.seller.SellerResponse;
import org.example.campaigntask.model.seller.Seller;
import org.example.campaigntask.repository.seller.SellerRepository;
import org.springframework.stereotype.Service;

@Service
public class SellerService {
    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public SellerResponse getCurrentSellerResponse(){
        Seller seller= getCurrentSeller();
        return new SellerResponse(seller.getId(),seller.getName(),seller.getEmeraldBalance());
    }

    public Seller getCurrentSeller(){
        return sellerRepository.findFirstByOrderByIdAsc().orElseThrow();
    }
}
