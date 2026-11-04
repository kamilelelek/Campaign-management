package org.example.campaigntask.service.catalog;


import org.example.campaigntask.dto.catalog.ProductResponse;
import org.example.campaigntask.dto.catalog.TownResponse;
import org.example.campaigntask.repository.catalog.KeywordRepository;
import org.example.campaigntask.repository.catalog.ProductRepository;
import org.example.campaigntask.repository.catalog.TownRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final KeywordRepository keywordRepository;
    private final ProductRepository productRepository;
    private final TownRepository townRepository;

    public CatalogService(KeywordRepository keywordRepository, ProductRepository productRepository, TownRepository townRepository) {
        this.keywordRepository = keywordRepository;
        this.productRepository = productRepository;
        this.townRepository = townRepository;
    }
    public List<ProductResponse> getProducts(){
        return productRepository.findAll(Sort.by("id")).stream().map(product->new ProductResponse(
                product.getId(),
                product.getName()
        )).toList();
    }
    public List<TownResponse> getTowns(){
        return townRepository.findAll(Sort.by("id")).stream()
                .map(town -> new TownResponse(town.getId(),town.getName())).toList();
    }
}
