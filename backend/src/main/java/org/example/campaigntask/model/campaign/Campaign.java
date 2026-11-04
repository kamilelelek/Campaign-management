package org.example.campaigntask.model.campaign;

import jakarta.persistence.*;
import lombok.*;
import org.example.campaigntask.model.catalog.Keyword;
import org.example.campaigntask.model.catalog.Product;
import org.example.campaigntask.model.catalog.Town;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bidAmount;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fund;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;
    @Column(nullable = false)
    private Integer radiusKm;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "town_id")
    private Town town;
    @ManyToMany
    @JoinTable(name = "campaign_keyword",
            joinColumns = @JoinColumn(name = "campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @Builder.Default
    private Set<Keyword> keywords = new HashSet<>();
}
