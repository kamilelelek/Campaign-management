package org.example.campaigntask.dto.campaign;

import jakarta.validation.constraints.*;
import org.example.campaigntask.model.campaign.CampaignStatus;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateCampaignRequest(@NotBlank String name,
                                    @NotEmpty Set<Long> keywordIds,
                                    @NotNull @DecimalMin("0.1") @Digits(integer = 10, fraction = 2) BigDecimal bidAmount,
                                    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal fund,
                                    @NotNull CampaignStatus status,
                                    Long townId,
                                    @NotNull @Positive Integer radiusKm) {
}
