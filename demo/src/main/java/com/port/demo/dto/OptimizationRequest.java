package com.port.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRequest {

    @NotNull(message = "Portfolio ID is required")
    private Long portfolioId;

    @Positive(message = "Alpha must be positive if provided")
    private Double alphaOverride;

    @Positive(message = "Lookback period must be positive")
    @Builder.Default
    private Integer lookbackDays = 252;
}
