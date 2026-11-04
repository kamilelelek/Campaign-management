package org.example.campaigntask.controller.catalog;

import org.example.campaigntask.dto.catalog.ProductResponse;
import org.example.campaigntask.dto.catalog.TownResponse;
import org.example.campaigntask.service.catalog.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }
    @GetMapping("/products")
    public List<ProductResponse> getAllProducts(){
        return catalogService.getProducts();
    }
    @GetMapping("/towns")
    public List<TownResponse> getAllTowns(){
        return catalogService.getTowns();
    }
}
