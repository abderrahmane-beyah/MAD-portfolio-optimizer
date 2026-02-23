package com.port.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {
    private Long id;
    private String symbol;
    private String name;
    private String sector;
    private String assetClass;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer historicalDataPoints;
}
