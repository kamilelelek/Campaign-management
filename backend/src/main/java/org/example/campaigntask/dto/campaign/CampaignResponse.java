package org.example.campaigntask.dto.campaign;

import org.example.campaigntask.dto.catalog.KeywordResponse;
import org.example.campaigntask.model.campaign.CampaignStatus;

import java.math.BigDecimal;
import java.util.List;

public record CampaignResponse(Long id,
                               String name,
                               BigDecimal bidAmount,
                               BigDecimal fund,
                               CampaignStatus status,
                               Integer radiusKm,
                               Long productId,
                               String productName,
                               Long townId,
                               String townName,
                               List<KeywordResponse> keywords) {
}