package com.port.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private Double defaultAlpha;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PortfolioAssetResponse> assets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioAssetResponse {
        private Long assetId;
        private String assetSymbol;
        private String assetName;
        private Double minWeight;
        private Double maxWeight;
    }
}
