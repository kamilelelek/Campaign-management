package org.example.campaigntask.service.campaign;

import org.example.campaigntask.dto.campaign.CampaignResponse;
import org.example.campaigntask.dto.campaign.CreateCampaignRequest;
import org.example.campaigntask.dto.campaign.UpdateCampaignRequest;
import org.example.campaigntask.dto.catalog.KeywordResponse;
import org.example.campaigntask.model.campaign.Campaign;
import org.example.campaigntask.model.catalog.Keyword;
import org.example.campaigntask.model.catalog.Product;
import org.example.campaigntask.model.catalog.Town;
import org.example.campaigntask.model.seller.Seller;
import org.example.campaigntask.repository.campaign.CampaignRepository;
import org.example.campaigntask.repository.catalog.KeywordRepository;
import org.example.campaigntask.repository.catalog.ProductRepository;
import org.example.campaigntask.repository.catalog.TownRepository;
import org.example.campaigntask.service.seller.SellerService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final KeywordRepository keywordRepository;
    private final TownRepository townRepository;
    private final SellerService sellerService;

    public CampaignService(CampaignRepository campaignRepository, ProductRepository productRepository, KeywordRepository keywordRepository, TownRepository townRepository, SellerService sellerService) {
        this.campaignRepository = campaignRepository;
        this.productRepository = productRepository;
        this.keywordRepository = keywordRepository;
        this.townRepository = townRepository;
        this.sellerService = sellerService;
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getAll() {
        return campaignRepository.findAll(Sort.by("id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CampaignResponse create(CreateCampaignRequest createCampaignRequest) {
        Product product = productRepository.findById(createCampaignRequest.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (campaignRepository.existsByProductId(product.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This product already has a campaign");
        }
        Set<Keyword> keywords = resolveKeywords(createCampaignRequest.keywordIds());
        Town town = resolveTown(createCampaignRequest.townId());

        chargeSeller(sellerService.getCurrentSeller(), createCampaignRequest.fund());
        Campaign campaign = Campaign.builder()
                .product(product)
                .name(createCampaignRequest.name())
                .keywords(keywords)
                .bidAmount(createCampaignRequest.bidAmount())
                .fund(createCampaignRequest.fund())
                .status(createCampaignRequest.status())
                .town(town)
                .radiusKm(createCampaignRequest.radiusKm())
                .build();

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignResponse update(Long id, UpdateCampaignRequest updateCampaignRequest) {
        Campaign campaign = findCampaign(id);
        Set<Keyword> keywords = resolveKeywords(updateCampaignRequest.keywordIds());
        Town town = resolveTown(updateCampaignRequest.townId());

        BigDecimal difference = updateCampaignRequest.fund().subtract(campaign.getFund());
        chargeSeller(sellerService.getCurrentSeller(), difference);
        campaign.setName(updateCampaignRequest.name());
        campaign.setKeywords(keywords);
        campaign.setBidAmount(updateCampaignRequest.bidAmount());
        campaign.setFund(updateCampaignRequest.fund());
        campaign.setStatus(updateCampaignRequest.status());
        campaign.setTown(town);
        campaign.setRadiusKm(updateCampaignRequest.radiusKm());

        return toResponse(campaign);
    }

    @Transactional
    public void delete(Long id) {
        Campaign campaign = findCampaign(id);
        Seller seller = sellerService.getCurrentSeller();
        seller.setEmeraldBalance(seller.getEmeraldBalance().add(campaign.getFund()));
        campaignRepository.delete(campaign);
    }

    private void chargeSeller(Seller seller, BigDecimal amount) {
        if (amount.compareTo(seller.getEmeraldBalance()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
        seller.setEmeraldBalance(seller.getEmeraldBalance().subtract(amount));
    }

    private Campaign findCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    private Set<Keyword> resolveKeywords(Set<Long> keywordIds) {
        List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
        if (keywords.size() != keywordIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown keyword");
        }
        return new HashSet<>(keywords);
    }

    private Town resolveTown(Long townId) {
        if (townId == null) return null;
        return townRepository.findById(townId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Town not found"));
    }

    private CampaignResponse toResponse(Campaign campaign) {
        Town town = campaign.getTown();
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getBidAmount(),
                campaign.getFund(),
                campaign.getStatus(),
                campaign.getRadiusKm(),
                campaign.getProduct().getId(),
                campaign.getProduct().getName(),
                town != null ? town.getId() : null,
                town != null ? town.getName() : null,
                campaign.getKeywords().stream()
                        .sorted(Comparator.comparing(Keyword::getName))
                        .map(keyword -> new KeywordResponse(keyword.getId(), keyword.getName()))
                        .toList()
        );
    }
}
