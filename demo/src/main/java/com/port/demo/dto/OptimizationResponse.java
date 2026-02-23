package com.port.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResponse {
    private Long id;
    private Long portfolioId;
    private Double alphaUsed;
    private Double expectedReturn;
    private Double riskMad;
    private Double objectiveValue;
    private Map<String, Double> weights;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
