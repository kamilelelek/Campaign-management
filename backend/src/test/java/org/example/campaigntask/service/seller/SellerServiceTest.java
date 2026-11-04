package org.example.campaigntask.service.seller;

import org.example.campaigntask.dto.seller.SellerResponse;
import org.example.campaigntask.model.seller.Seller;
import org.example.campaigntask.repository.seller.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    void getCurrentSeller_returnsFirstSeller() {
        Seller seller = seller();
        when(sellerRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(seller));

        assertThat(sellerService.getCurrentSeller()).isSameAs(seller);
    }

    @Test
    void getCurrentSeller_throws_whenNoSellerExists() {
        when(sellerRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getCurrentSeller()).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getCurrentSellerResponse_mapsSellerToResponse() {
        when(sellerRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(seller()));

        SellerResponse response = sellerService.getCurrentSellerResponse();

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Smith");
        assertThat(response.emeraldBalance()).isEqualByComparingTo("1000.00");
    }

    private Seller seller() {
        Seller seller = new Seller();
        seller.setId(1L);
        seller.setName("John Smith");
        seller.setEmeraldBalance(new BigDecimal("1000.00"));
        return seller;
    }
}
