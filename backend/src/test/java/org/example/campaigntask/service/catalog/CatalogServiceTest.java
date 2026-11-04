package org.example.campaigntask.service.catalog;

import org.example.campaigntask.dto.catalog.KeywordResponse;
import org.example.campaigntask.dto.catalog.ProductResponse;
import org.example.campaigntask.dto.catalog.TownResponse;
import org.example.campaigntask.model.catalog.Keyword;
import org.example.campaigntask.model.catalog.Product;
import org.example.campaigntask.model.catalog.Town;
import org.example.campaigntask.repository.catalog.KeywordRepository;
import org.example.campaigntask.repository.catalog.ProductRepository;
import org.example.campaigntask.repository.catalog.TownRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private TownRepository townRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void getProducts_returnsMappedProductsSortedById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Lenovo Laptop");
        when(productRepository.findAll(Sort.by("id"))).thenReturn(List.of(product));

        assertThat(catalogService.getProducts()).containsExactly(new ProductResponse(1L, "Lenovo Laptop"));
    }

    @Test
    void getTowns_returnsMappedTownsSortedById() {
        Town town = new Town();
        town.setId(1L);
        town.setName("Warsaw");
        when(townRepository.findAll(Sort.by("id"))).thenReturn(List.of(town));

        assertThat(catalogService.getTowns()).containsExactly(new TownResponse(1L, "Warsaw"));
    }

    @Test
    void getKeywords_passesQueryToRepositoryAndMapsResults() {
        Keyword keyword = new Keyword();
        keyword.setId(3L);
        keyword.setName("laptop");
        when(keywordRepository.findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc("lap")).thenReturn(List.of(keyword));

        assertThat(catalogService.getKeywords("lap")).containsExactly(new KeywordResponse(3L, "laptop"));
    }

    @Test
    void getKeywords_returnsEmptyList_whenNothingMatches() {
        when(keywordRepository.findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc("xyz")).thenReturn(List.of());

        assertThat(catalogService.getKeywords("xyz")).isEmpty();
    }
}
