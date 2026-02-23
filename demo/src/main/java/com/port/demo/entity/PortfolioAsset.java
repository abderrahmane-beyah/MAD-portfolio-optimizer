package com.port.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "portfolio_assets",
       uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "asset_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    @Builder.Default
    private Double minWeight = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double maxWeight = 1.0;
}
