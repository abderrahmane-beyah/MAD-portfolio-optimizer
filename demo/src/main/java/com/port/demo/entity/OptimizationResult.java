package com.port.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "optimization_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private Double alphaUsed;

    @Column(nullable = false)
    private Double expectedReturn;

    @Column(nullable = false)
    private Double riskMad;

    @Column(nullable = false)
    private Double objectiveValue;

    @ElementCollection
    @CollectionTable(name = "optimization_weights",
                     joinColumns = @JoinColumn(name = "optimization_result_id"))
    @MapKeyColumn(name = "asset_symbol")
    @Column(name = "weight")
    @Builder.Default
    private Map<String, Double> weights = new HashMap<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptimizationStatus status;

    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum OptimizationStatus {
        SUCCESS,
        FAILED,
        INFEASIBLE
    }
}
