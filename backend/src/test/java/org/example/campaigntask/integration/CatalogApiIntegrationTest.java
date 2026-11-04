package org.example.campaigntask.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSeller_returnsSeededSeller() throws Exception {
        mockMvc.perform(get("/api/seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.emeraldBalance").isNumber());
    }

    @Test
    void getProducts_returnsSeededProductsSortedById() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Lenovo Laptop"));
    }

    @Test
    void getTowns_returnsSeededTownsSortedById() throws Exception {
        mockMvc.perform(get("/api/towns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Warsaw"));
    }

    @Test
    void getKeywords_returnsOnlyKeywordsStartingWithQuery() throws Exception {
        mockMvc.perform(get("/api/keywords").param("query", "e"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("electronics"))
                .andExpect(jsonPath("$[1].name").value("equipment"));
    }

    @Test
    void getKeywords_ignoresCaseWhenFiltering() throws Exception {
        mockMvc.perform(get("/api/keywords").param("query", "LAP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("laptop"));
    }

    @Test
    void getKeywords_returnsAtMostTenSortedResults_whenQueryIsMissing() throws Exception {
        mockMvc.perform(get("/api/keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].name").value("accessories"));
    }

    @Test
    void getKeywords_returnsEmptyList_whenNothingMatches() throws Exception {
        mockMvc.perform(get("/api/keywords").param("query", "no-such-keyword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
