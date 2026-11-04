package org.example.campaigntask.service.campaign;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.example.campaigntask.dto.campaign.CampaignResponse;
import org.example.campaigntask.dto.campaign.CreateCampaignRequest;
import org.example.campaigntask.dto.campaign.UpdateCampaignRequest;
import org.example.campaigntask.dto.catalog.KeywordResponse;
import org.example.campaigntask.model.campaign.Campaign;
import org.example.campaigntask.model.campaign.CampaignStatus;
import org.example.campaigntask.model.catalog.Keyword;
import org.example.campaigntask.model.catalog.Product;
import org.example.campaigntask.model.catalog.Town;
import org.example.campaigntask.model.seller.Seller;
import org.example.campaigntask.repository.campaign.CampaignRepository;
import org.example.campaigntask.repository.catalog.KeywordRepository;
import org.example.campaigntask.repository.catalog.ProductRepository;
import org.example.campaigntask.repository.catalog.TownRepository;
import org.example.campaigntask.service.seller.SellerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long KEYWORD_ID = 1L;
    private static final Long TOWN_ID = 1L;
    private static final Long CAMPAIGN_ID = 1L;

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private TownRepository townRepository;
    @Mock
    private SellerService sellerService;

    @InjectMocks
    private CampaignService campaignService;

    @Test
    void getAll_returnsMappedCampaignsSortedById() {
        Campaign campaign = campaign("100.00");
        when(campaignRepository.findAll(Sort.by("id"))).thenReturn(List.of(campaign));

        List<CampaignResponse> responses = campaignService.getAll();

        assertThat(responses).hasSize(1);
        CampaignResponse response = responses.get(0);
        assertThat(response.id()).isEqualTo(CAMPAIGN_ID);
        assertThat(response.name()).isEqualTo("Old name");
        assertThat(response.productId()).isEqualTo(PRODUCT_ID);
        assertThat(response.productName()).isEqualTo("Lenovo Laptop");
        assertThat(response.townId()).isEqualTo(TOWN_ID);
        assertThat(response.townName()).isEqualTo("Warsaw");
    }

    @Test
    void getAll_returnsNullTownFields_whenCampaignHasNoTown() {
        Campaign campaign = campaign("100.00");
        campaign.setTown(null);
        when(campaignRepository.findAll(Sort.by("id"))).thenReturn(List.of(campaign));

        CampaignResponse response = campaignService.getAll().get(0);

        assertThat(response.townId()).isNull();
        assertThat(response.townName()).isNull();
    }

    @Test
    void create_chargesSellerAndSavesCampaign_whenRequestIsValid() {
        Seller seller = seller("1000.00");
        stubValidCreate(seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));
        when(campaignRepository.save(any(Campaign.class))).then(returnsFirstArg());

        CampaignResponse response = campaignService.create(createRequest("100.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("900.00");
        assertThat(response.name()).isEqualTo("Laptop campaign");
        assertThat(response.bidAmount()).isEqualByComparingTo("1.00");
        assertThat(response.fund()).isEqualByComparingTo("100.00");
        assertThat(response.status()).isEqualTo(CampaignStatus.ON);
        assertThat(response.radiusKm()).isEqualTo(10);
        assertThat(response.productId()).isEqualTo(PRODUCT_ID);
        assertThat(response.townName()).isEqualTo("Warsaw");
        assertThat(response.keywords()).containsExactly(new KeywordResponse(KEYWORD_ID, "laptop"));
    }

    @Test
    void create_savesCampaignWithRequestData() {
        stubValidCreate(seller("1000.00"));
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));
        when(campaignRepository.save(any(Campaign.class))).then(returnsFirstArg());

        campaignService.create(createRequest("100.00", TOWN_ID));

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        Campaign saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Laptop campaign");
        assertThat(saved.getFund()).isEqualByComparingTo("100.00");
        assertThat(saved.getProduct().getId()).isEqualTo(PRODUCT_ID);
        assertThat(saved.getTown().getId()).isEqualTo(TOWN_ID);
        assertThat(saved.getKeywords()).hasSize(1);
    }

    @Test
    void create_allowsFundEqualToBalance() {
        Seller seller = seller("100.00");
        stubValidCreate(seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));
        when(campaignRepository.save(any(Campaign.class))).then(returnsFirstArg());

        campaignService.create(createRequest("100.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void create_succeedsWithoutTown() {
        stubValidCreate(seller("1000.00"));
        when(campaignRepository.save(any(Campaign.class))).then(returnsFirstArg());

        CampaignResponse response = campaignService.create(createRequest("100.00", null));

        assertThat(response.townId()).isNull();
        assertThat(response.townName()).isNull();
        verifyNoInteractions(townRepository);
    }

    @Test
    void create_returnsKeywordsSortedByName() {
        Keyword zebra = keyword(1L, "zebra");
        Keyword apple = keyword(2L, "apple");
        Set<Long> keywordIds = Set.of(1L, 2L);
        CreateCampaignRequest request = new CreateCampaignRequest(
                "Laptop campaign", PRODUCT_ID, keywordIds, new BigDecimal("1.00"),
                new BigDecimal("100.00"), CampaignStatus.ON, null, 10);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(campaignRepository.existsByProductId(PRODUCT_ID)).thenReturn(false);
        when(keywordRepository.findAllById(keywordIds)).thenReturn(List.of(zebra, apple));
        when(sellerService.getCurrentSeller()).thenReturn(seller("1000.00"));
        when(campaignRepository.save(any(Campaign.class))).then(returnsFirstArg());

        CampaignResponse response = campaignService.create(request);

        assertThat(response.keywords()).extracting(KeywordResponse::name).containsExactly("apple", "zebra");
    }

    @Test
    void create_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertStatus(() -> campaignService.create(createRequest("100.00", TOWN_ID)), HttpStatus.NOT_FOUND);

        verify(campaignRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    @Test
    void create_throwsConflict_whenProductAlreadyHasCampaign() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(campaignRepository.existsByProductId(PRODUCT_ID)).thenReturn(true);

        assertStatus(() -> campaignService.create(createRequest("100.00", TOWN_ID)), HttpStatus.CONFLICT);

        verify(campaignRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    @Test
    void create_throwsBadRequest_whenKeywordIsUnknown() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(campaignRepository.existsByProductId(PRODUCT_ID)).thenReturn(false);
        when(keywordRepository.findAllById(Set.of(KEYWORD_ID))).thenReturn(List.of());

        assertStatus(() -> campaignService.create(createRequest("100.00", TOWN_ID)), HttpStatus.BAD_REQUEST);

        verify(campaignRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    @Test
    void create_throwsNotFound_whenTownDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(campaignRepository.existsByProductId(PRODUCT_ID)).thenReturn(false);
        when(keywordRepository.findAllById(Set.of(KEYWORD_ID))).thenReturn(List.of(keyword(KEYWORD_ID, "laptop")));
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.empty());

        assertStatus(() -> campaignService.create(createRequest("100.00", TOWN_ID)), HttpStatus.NOT_FOUND);

        verify(campaignRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    @Test
    void create_throwsConflictAndKeepsBalance_whenFundsAreInsufficient() {
        Seller seller = seller("50.00");
        stubValidCreate(seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        assertStatus(() -> campaignService.create(createRequest("100.00", TOWN_ID)), HttpStatus.CONFLICT);

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("50.00");
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void update_chargesOnlyDifference_whenFundIncreases() {
        Seller seller = seller("1000.00");
        Campaign campaign = campaign("100.00");
        stubValidUpdate(campaign, seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        CampaignResponse response = campaignService.update(CAMPAIGN_ID, updateRequest("150.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("950.00");
        assertThat(campaign.getFund()).isEqualByComparingTo("150.00");
        assertThat(response.fund()).isEqualByComparingTo("150.00");
    }

    @Test
    void update_refundsDifference_whenFundDecreases() {
        Seller seller = seller("1000.00");
        Campaign campaign = campaign("100.00");
        stubValidUpdate(campaign, seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        campaignService.update(CAMPAIGN_ID, updateRequest("40.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("1060.00");
        assertThat(campaign.getFund()).isEqualByComparingTo("40.00");
    }

    @Test
    void update_keepsBalance_whenFundIsUnchanged() {
        Seller seller = seller("1000.00");
        stubValidUpdate(campaign("100.00"), seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        campaignService.update(CAMPAIGN_ID, updateRequest("100.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void update_allowsRefund_whenBalanceIsZero() {
        Seller seller = seller("0.00");
        stubValidUpdate(campaign("100.00"), seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        campaignService.update(CAMPAIGN_ID, updateRequest("60.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("40.00");
    }

    @Test
    void update_allowsDifferenceEqualToBalance() {
        Seller seller = seller("50.00");
        stubValidUpdate(campaign("100.00"), seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        campaignService.update(CAMPAIGN_ID, updateRequest("150.00", TOWN_ID));

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void update_changesAllEditableFields() {
        Campaign campaign = campaign("100.00");
        stubValidUpdate(campaign, seller("1000.00"));
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        campaignService.update(CAMPAIGN_ID, updateRequest("100.00", TOWN_ID));

        assertThat(campaign.getName()).isEqualTo("New name");
        assertThat(campaign.getBidAmount()).isEqualByComparingTo("2.00");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.OFF);
        assertThat(campaign.getRadiusKm()).isEqualTo(20);
        assertThat(campaign.getProduct().getId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void update_clearsTown_whenTownIdIsNull() {
        Campaign campaign = campaign("100.00");
        stubValidUpdate(campaign, seller("1000.00"));

        CampaignResponse response = campaignService.update(CAMPAIGN_ID, updateRequest("100.00", null));

        assertThat(campaign.getTown()).isNull();
        assertThat(response.townName()).isNull();
        verifyNoInteractions(townRepository);
    }

    @Test
    void update_throwsConflictAndKeepsState_whenDifferenceExceedsBalance() {
        Seller seller = seller("10.00");
        Campaign campaign = campaign("100.00");
        stubValidUpdate(campaign, seller);
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.of(town()));

        assertStatus(() -> campaignService.update(CAMPAIGN_ID, updateRequest("150.00", TOWN_ID)), HttpStatus.CONFLICT);

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("10.00");
        assertThat(campaign.getFund()).isEqualByComparingTo("100.00");
        assertThat(campaign.getName()).isEqualTo("Old name");
    }

    @Test
    void update_throwsNotFound_whenCampaignDoesNotExist() {
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.empty());

        assertStatus(() -> campaignService.update(CAMPAIGN_ID, updateRequest("150.00", TOWN_ID)), HttpStatus.NOT_FOUND);

        verifyNoInteractions(sellerService);
    }

    @Test
    void update_throwsBadRequestAndKeepsFund_whenKeywordIsUnknown() {
        Campaign campaign = campaign("100.00");
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(keywordRepository.findAllById(Set.of(KEYWORD_ID))).thenReturn(List.of());

        assertStatus(() -> campaignService.update(CAMPAIGN_ID, updateRequest("150.00", TOWN_ID)), HttpStatus.BAD_REQUEST);

        assertThat(campaign.getFund()).isEqualByComparingTo("100.00");
        verifyNoInteractions(sellerService);
    }

    @Test
    void update_throwsNotFoundAndKeepsFund_whenTownDoesNotExist() {
        Campaign campaign = campaign("100.00");
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(keywordRepository.findAllById(Set.of(KEYWORD_ID))).thenReturn(List.of(keyword(KEYWORD_ID, "laptop")));
        when(townRepository.findById(TOWN_ID)).thenReturn(Optional.empty());

        assertStatus(() -> campaignService.update(CAMPAIGN_ID, updateRequest("150.00", TOWN_ID)), HttpStatus.NOT_FOUND);

        assertThat(campaign.getFund()).isEqualByComparingTo("100.00");
        verifyNoInteractions(sellerService);
    }

    @Test
    void delete_refundsWholeFundAndRemovesCampaign() {
        Seller seller = seller("900.00");
        Campaign campaign = campaign("100.00");
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(sellerService.getCurrentSeller()).thenReturn(seller);

        campaignService.delete(CAMPAIGN_ID);

        assertThat(seller.getEmeraldBalance()).isEqualByComparingTo("1000.00");
        verify(campaignRepository).delete(campaign);
    }

    @Test
    void delete_throwsNotFound_whenCampaignDoesNotExist() {
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.empty());

        assertStatus(() -> campaignService.delete(CAMPAIGN_ID), HttpStatus.NOT_FOUND);

        verify(campaignRepository, never()).delete(any());
        verifyNoInteractions(sellerService);
    }

    private void stubValidCreate(Seller seller) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(campaignRepository.existsByProductId(PRODUCT_ID)).thenReturn(false);
        when(keywordRepository.findAllById(Set.of(KEYWORD_ID))).thenReturn(List.of(keyword(KEYWORD_ID, "laptop")));
        when(sellerService.getCurrentSeller()).thenReturn(seller);
    }

    private void stubValidUpdate(Campaign campaign, Seller seller) {
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(keywordRepository.findAllById(Set.of(KEYWORD_ID))).thenReturn(List.of(keyword(KEYWORD_ID, "laptop")));
        when(sellerService.getCurrentSeller()).thenReturn(seller);
    }

    private void assertStatus(ThrowingCallable call, HttpStatus status) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(status));
    }

    private CreateCampaignRequest createRequest(String fund, Long townId) {
        return new CreateCampaignRequest(
                "Laptop campaign",
                PRODUCT_ID,
                Set.of(KEYWORD_ID),
                new BigDecimal("1.00"),
                new BigDecimal(fund),
                CampaignStatus.ON,
                townId,
                10
        );
    }

    private UpdateCampaignRequest updateRequest(String fund, Long townId) {
        return new UpdateCampaignRequest(
                "New name",
                Set.of(KEYWORD_ID),
                new BigDecimal("2.00"),
                new BigDecimal(fund),
                CampaignStatus.OFF,
                townId,
                20
        );
    }

    private Campaign campaign(String fund) {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .name("Old name")
                .bidAmount(new BigDecimal("1.00"))
                .fund(new BigDecimal(fund))
                .status(CampaignStatus.ON)
                .radiusKm(10)
                .product(product())
                .town(town())
                .keywords(new HashSet<>(Set.of(keyword(KEYWORD_ID, "laptop"))))
                .build();
    }

    private Seller seller(String balance) {
        Seller seller = new Seller();
        seller.setId(1L);
        seller.setName("John Smith");
        seller.setEmeraldBalance(new BigDecimal(balance));
        return seller;
    }

    private Product product() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName("Lenovo Laptop");
        return product;
    }

    private Keyword keyword(Long id, String name) {
        Keyword keyword = new Keyword();
        keyword.setId(id);
        keyword.setName(name);
        return keyword;
    }

    private Town town() {
        Town town = new Town();
        town.setId(TOWN_ID);
        town.setName("Warsaw");
        return town;
    }
}
