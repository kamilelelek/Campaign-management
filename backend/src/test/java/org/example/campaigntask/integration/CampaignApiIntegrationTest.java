package org.example.campaigntask.integration;

import com.jayway.jsonpath.JsonPath;
import org.example.campaigntask.model.seller.Seller;
import org.example.campaigntask.repository.campaign.CampaignRepository;
import org.example.campaigntask.repository.seller.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampaignApiIntegrationTest {

    private static final String CAMPAIGNS_URL = "/api/campaigns";
    private static final Long UNKNOWN_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private SellerRepository sellerRepository;

    @BeforeEach
    void resetState() {
        campaignRepository.deleteAll();
        Seller seller = sellerRepository.findFirstByOrderByIdAsc().orElseThrow();
        seller.setEmeraldBalance(new BigDecimal("1000.00"));
        sellerRepository.save(seller);
    }

    @Test
    void getCampaigns_returnsEmptyList_whenNoCampaignExists() throws Exception {
        mockMvc.perform(get(CAMPAIGNS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void postCampaign_createsCampaignAndChargesSeller() throws Exception {
        postCampaign(campaignBody(1, "100.00", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test campaign"))
                .andExpect(jsonPath("$.bidAmount").value(1.00))
                .andExpect(jsonPath("$.fund").value(100.00))
                .andExpect(jsonPath("$.status").value("ON"))
                .andExpect(jsonPath("$.radiusKm").value(10))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Lenovo Laptop"))
                .andExpect(jsonPath("$.townId").value(1))
                .andExpect(jsonPath("$.townName").value("Warsaw"))
                .andExpect(jsonPath("$.keywords", hasSize(2)));

        assertBalance(900.00);
        mockMvc.perform(get(CAMPAIGNS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productName").value("Lenovo Laptop"));
    }

    @Test
    void postCampaign_allowsFundEqualToBalance() throws Exception {
        postCampaign(campaignBody(1, "1000.00", "1"))
                .andExpect(status().isCreated());

        assertBalance(0.00);
    }

    @Test
    void postCampaign_acceptsMinimumBid() throws Exception {
        postCampaign("""
                {"name":"Min bid","productId":1,"keywordIds":[1],"bidAmount":0.10,"fund":10.00,"status":"ON","radiusKm":10}
                """)
                .andExpect(status().isCreated());
    }

    @Test
    void campaignWithoutTown_isCreatedAndListed() throws Exception {
        postCampaign(campaignBody(1, "100.00", "null"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.townId").doesNotExist());

        mockMvc.perform(get(CAMPAIGNS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].townName").doesNotExist());
    }

    @Test
    void postCampaign_returnsConflictAndKeepsBalance_whenProductAlreadyHasCampaign() throws Exception {
        createCampaign(1, "100.00");

        postCampaign(campaignBody(1, "50.00", "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This product already has a campaign"));

        assertBalance(900.00);
        assertCampaignCount(1);
    }

    @Test
    void postCampaign_returnsConflictAndCreatesNothing_whenFundsAreInsufficient() throws Exception {
        postCampaign(campaignBody(1, "1000.01", "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        assertBalance(1000.00);
        assertCampaignCount(0);
    }

    @Test
    void postCampaign_returnsNotFound_whenProductDoesNotExist() throws Exception {
        postCampaign(campaignBody(UNKNOWN_ID, "100.00", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        assertBalance(1000.00);
    }

    @Test
    void postCampaign_returnsNotFound_whenTownDoesNotExist() throws Exception {
        postCampaign(campaignBody(1, "100.00", UNKNOWN_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Town not found"));

        assertBalance(1000.00);
        assertCampaignCount(0);
    }

    @Test
    void postCampaign_returnsBadRequest_whenKeywordIsUnknown() throws Exception {
        postCampaign("""
                {"name":"Test","productId":1,"keywordIds":[9999],"bidAmount":1.00,"fund":100.00,"status":"ON","radiusKm":10}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown keyword"));

        assertBalance(1000.00);
        assertCampaignCount(0);
    }

    @Test
    void postCampaign_returnsFieldErrors_whenFieldsAreInvalid() throws Exception {
        postCampaign("""
                {"name":"","keywordIds":[],"bidAmount":0.05,"fund":-1,"radiusKm":0}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.productId").exists())
                .andExpect(jsonPath("$.fieldErrors.keywordIds").exists())
                .andExpect(jsonPath("$.fieldErrors.bidAmount").exists())
                .andExpect(jsonPath("$.fieldErrors.fund").exists())
                .andExpect(jsonPath("$.fieldErrors.status").exists())
                .andExpect(jsonPath("$.fieldErrors.radiusKm").exists());

        assertBalance(1000.00);
    }

    @Test
    void postCampaign_returnsBadRequest_whenOneFieldBreaksTwoConstraints() throws Exception {
        postCampaign("""
                {"name":"Test","productId":1,"keywordIds":[1],"bidAmount":0.055,"fund":10.00,"status":"ON","radiusKm":10}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bidAmount").exists());
    }

    @Test
    void postCampaign_returnsBadRequest_whenFundHasTooManyDecimals() throws Exception {
        postCampaign(campaignBody(1, "10.555", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fund").exists());

        assertBalance(1000.00);
    }

    @Test
    void postCampaign_returnsBadRequest_whenStatusIsUnknown() throws Exception {
        postCampaign("""
                {"name":"Test","productId":1,"keywordIds":[1],"bidAmount":1.00,"fund":10.00,"status":"MAYBE","radiusKm":10}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));
    }

    @Test
    void postCampaign_returnsBadRequest_whenJsonIsMalformed() throws Exception {
        postCampaign("{\"name\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));
    }

    @Test
    void putCampaign_updatesFieldsAndChargesDifference() throws Exception {
        long id = createCampaign(1, "100.00");

        putCampaign(id, updateBody("150.00", "2", "[2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.bidAmount").value(2.00))
                .andExpect(jsonPath("$.fund").value(150.00))
                .andExpect(jsonPath("$.status").value("OFF"))
                .andExpect(jsonPath("$.radiusKm").value(20))
                .andExpect(jsonPath("$.townId").value(2))
                .andExpect(jsonPath("$.keywords", hasSize(1)))
                .andExpect(jsonPath("$.keywords[0].id").value(2));

        assertBalance(850.00);
        mockMvc.perform(get(CAMPAIGNS_URL))
                .andExpect(jsonPath("$[0].name").value("Updated"))
                .andExpect(jsonPath("$[0].fund").value(150.00))
                .andExpect(jsonPath("$[0].keywords", hasSize(1)));
    }

    @Test
    void putCampaign_refundsDifference_whenFundDecreases() throws Exception {
        long id = createCampaign(1, "100.00");

        putCampaign(id, updateBody("40.00", "1", "[1]"))
                .andExpect(status().isOk());

        assertBalance(960.00);
    }

    @Test
    void putCampaign_returnsConflictAndKeepsState_whenDifferenceExceedsBalance() throws Exception {
        long id = createCampaign(1, "100.00");

        putCampaign(id, updateBody("1000.01", "1", "[1]"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));

        assertBalance(900.00);
        mockMvc.perform(get(CAMPAIGNS_URL))
                .andExpect(jsonPath("$[0].name").value("Test campaign"))
                .andExpect(jsonPath("$[0].fund").value(100.00));
    }

    @Test
    void putCampaign_returnsBadRequestAndKeepsBalance_whenFundIsNegative() throws Exception {
        long id = createCampaign(1, "100.00");

        putCampaign(id, updateBody("-500.00", "1", "[1]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fund").exists());

        assertBalance(900.00);
    }

    @Test
    void putCampaign_returnsNotFound_whenCampaignDoesNotExist() throws Exception {
        putCampaign(UNKNOWN_ID, updateBody("100.00", "1", "[1]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Campaign not found"));

        assertBalance(1000.00);
    }

    @Test
    void deleteCampaign_refundsWholeFundAndRemovesCampaign() throws Exception {
        long id = createCampaign(1, "100.00");

        mockMvc.perform(delete(CAMPAIGNS_URL + "/" + id))
                .andExpect(status().isNoContent());

        assertBalance(1000.00);
        assertCampaignCount(0);
    }

    @Test
    void deleteCampaign_returnsNotFound_whenCalledTwice() throws Exception {
        long id = createCampaign(1, "100.00");
        mockMvc.perform(delete(CAMPAIGNS_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(CAMPAIGNS_URL + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Campaign not found"));

        assertBalance(1000.00);
    }

    @Test
    void deleteCampaign_makesProductAvailableForNewCampaign() throws Exception {
        long id = createCampaign(1, "100.00");
        mockMvc.perform(delete(CAMPAIGNS_URL + "/" + id))
                .andExpect(status().isNoContent());

        postCampaign(campaignBody(1, "50.00", "1"))
                .andExpect(status().isCreated());

        assertBalance(950.00);
    }

    private long createCampaign(long productId, String fund) throws Exception {
        String response = postCampaign(campaignBody(productId, fund, "1"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions postCampaign(String body) throws Exception {
        return mockMvc.perform(post(CAMPAIGNS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions putCampaign(long id, String body) throws Exception {
        return mockMvc.perform(put(CAMPAIGNS_URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertBalance(double expected) throws Exception {
        mockMvc.perform(get("/api/seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emeraldBalance").value(expected));
    }

    private void assertCampaignCount(int expected) throws Exception {
        mockMvc.perform(get(CAMPAIGNS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expected)));
    }

    private String campaignBody(long productId, String fund, String townId) {
        return """
                {"name":"Test campaign","productId":%d,"keywordIds":[1,3],"bidAmount":1.00,"fund":%s,"status":"ON","townId":%s,"radiusKm":10}
                """.formatted(productId, fund, townId);
    }

    private String updateBody(String fund, String townId, String keywordIds) {
        return """
                {"name":"Updated","keywordIds":%s,"bidAmount":2.00,"fund":%s,"status":"OFF","townId":%s,"radiusKm":20}
                """.formatted(keywordIds, fund, townId);
    }
}
