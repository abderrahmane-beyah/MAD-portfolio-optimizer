package com.port.demo.service;

import com.port.demo.dto.CreatePortfolioRequest;
import com.port.demo.dto.OptimizationResponse;
import com.port.demo.dto.PortfolioResponse;
import com.port.demo.entity.*;
import com.port.demo.exception.ResourceNotFoundException;
import com.port.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final OptimizationResultRepository optimizationResultRepository;

    @Transactional
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        log.info("Creating portfolio: {}", request.getName());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Portfolio portfolio = Portfolio.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .defaultAlpha(request.getDefaultAlpha())
                .build();

        portfolio = portfolioRepository.save(portfolio);

        // Add assets with constraints
        if (request.getAssetConstraints() != null) {
            for (CreatePortfolioRequest.PortfolioAssetConstraint constraint : request.getAssetConstraints()) {
                Asset asset = assetRepository.findBySymbol(constraint.getAssetSymbol())
                        .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + constraint.getAssetSymbol()));

                PortfolioAsset portfolioAsset = PortfolioAsset.builder()
                        .portfolio(portfolio)
                        .asset(asset)
                        .minWeight(constraint.getMinWeight())
                        .maxWeight(constraint.getMaxWeight())
                        .build();

                portfolioAssetRepository.save(portfolioAsset);
            }
        }

        return mapToResponse(portfolioRepository.findByIdWithAssets(portfolio.getId()));
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findByIdWithAssets(id);
        if (portfolio == null) {
            throw new ResourceNotFoundException("Portfolio not found: " + id);
        }
        return mapToResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getPortfoliosByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePortfolio(Long id) {
        portfolioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<OptimizationResponse> getOptimizationHistory(Long portfolioId) {
        return optimizationResultRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId).stream()
                .map(this::mapToOptimizationResponse)
                .collect(Collectors.toList());
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        List<PortfolioResponse.PortfolioAssetResponse> assets = portfolio.getPortfolioAssets().stream()
                .map(pa -> PortfolioResponse.PortfolioAssetResponse.builder()
                        .assetId(pa.getAsset().getId())
                        .assetSymbol(pa.getAsset().getSymbol())
                        .assetName(pa.getAsset().getName())
                        .minWeight(pa.getMinWeight())
                        .maxWeight(pa.getMaxWeight())
                        .build())
                .collect(Collectors.toList());

        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .userId(portfolio.getUser().getId())
                .defaultAlpha(portfolio.getDefaultAlpha())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .assets(assets)
                .build();
    }

    private OptimizationResponse mapToOptimizationResponse(OptimizationResult result) {
        return OptimizationResponse.builder()
                .id(result.getId())
                .portfolioId(result.getPortfolio().getId())
                .alphaUsed(result.getAlphaUsed())
                .expectedReturn(result.getExpectedReturn())
                .riskMad(result.getRiskMad())
                .objectiveValue(result.getObjectiveValue())
                .weights(result.getWeights())
                .status(result.getStatus().name())
                .errorMessage(result.getErrorMessage())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
