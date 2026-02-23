package com.port.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    private String name;

    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Default alpha is required")
    @Positive(message = "Alpha must be positive")
    private Double defaultAlpha;

    private List<PortfolioAssetConstraint> assetConstraints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioAssetConstraint {
        @NotBlank(message = "Asset symbol is required")
        private String assetSymbol;

        @NotNull(message = "Min weight is required")
        private Double minWeight;

        @NotNull(message = "Max weight is required")
        private Double maxWeight;
    }
}
