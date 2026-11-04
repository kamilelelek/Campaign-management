package org.example.campaigntask.controller.seller;

import org.example.campaigntask.dto.seller.SellerResponse;
import org.example.campaigntask.service.seller.SellerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SellerController {
    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/seller")
    public SellerResponse getSeller(){return sellerService.getCurrentSellerResponse();}
}
