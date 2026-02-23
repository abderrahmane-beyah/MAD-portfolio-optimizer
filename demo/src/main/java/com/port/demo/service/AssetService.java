package com.port.demo.service;

import com.port.demo.dto.AssetResponse;
import com.port.demo.dto.CreateAssetRequest;
import com.port.demo.entity.Asset;
import com.port.demo.exception.DuplicateResourceException;
import com.port.demo.exception.ResourceNotFoundException;
import com.port.demo.repository.AssetRepository;
import com.port.demo.repository.HistoricalReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;
    private final HistoricalReturnRepository historicalReturnRepository;
    private final AlphaVantageService alphaVantageService;

    @Transactional
    public AssetResponse createAsset(CreateAssetRequest request) {
        log.info("Creating asset: {}", request.getSymbol());

        if (assetRepository.existsBySymbol(request.getSymbol())) {
            throw new DuplicateResourceException("Asset with symbol already exists: " + request.getSymbol());
        }

        Asset asset = Asset.builder()
                .symbol(request.getSymbol().toUpperCase())
                .name(request.getName())
                .sector(request.getSector())
                .assetClass(request.getAssetClass())
                .build();

        asset = assetRepository.save(asset);

        // Fetch historical data from AlphaVantage
        try {
            alphaVantageService.fetchAndStoreHistoricalData(asset);
        } catch (Exception e) {
            log.error("Failed to fetch historical data for asset: {}", asset.getSymbol(), e);
            // Don't fail the asset creation, just log the error
        }

        return mapToResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
        return mapToResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetBySymbol(String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + symbol));
        return mapToResponse(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void refreshHistoricalData(String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + symbol));

        alphaVantageService.fetchAndStoreHistoricalData(asset);
    }

    private AssetResponse mapToResponse(Asset asset) {
        Long dataPoints = historicalReturnRepository.countByAssetId(asset.getId());

        return AssetResponse.builder()
                .id(asset.getId())
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .sector(asset.getSector())
                .assetClass(asset.getAssetClass())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .historicalDataPoints(dataPoints.intValue())
                .build();
    }
}
