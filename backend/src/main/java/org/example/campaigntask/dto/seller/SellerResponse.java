package org.example.campaigntask.dto.seller;

import java.math.BigDecimal;

public record SellerResponse(Long id, String name, BigDecimal emeraldBalance) {
}
