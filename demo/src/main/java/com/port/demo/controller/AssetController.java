package com.port.demo.controller;

import com.port.demo.dto.AssetResponse;
import com.port.demo.dto.CreateAssetRequest;
import com.port.demo.dto.ErrorResponse;
import com.port.demo.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Asset management and historical data operations")
public class AssetController {

    private final AssetService assetService;

    @Operation(summary = "Create a new asset", description = "Registers a new financial asset (stock, ETF, etc.) with its symbol and metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asset created successfully",
                    content = @Content(schema = @Schema(implementation = AssetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Asset with this symbol already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody CreateAssetRequest request) {
        AssetResponse response = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get asset by ID", description = "Retrieves an asset by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset found",
                    content = @Content(schema = @Schema(implementation = AssetResponse.class))),
            @ApiResponse(responseCode = "404", description = "Asset not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAssetById(
            @Parameter(description = "Asset ID", required = true) @PathVariable Long id) {
        AssetResponse response = assetService.getAssetById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all assets", description = "Retrieves a list of all registered assets")
    @ApiResponse(responseCode = "200", description = "List of assets",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AssetResponse.class))))
    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAllAssets() {
        List<AssetResponse> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    @Operation(summary = "Get asset by symbol", description = "Retrieves an asset by its ticker symbol (e.g., AAPL, GOOGL)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset found",
                    content = @Content(schema = @Schema(implementation = AssetResponse.class))),
            @ApiResponse(responseCode = "404", description = "Asset not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<AssetResponse> getAssetBySymbol(
            @Parameter(description = "Ticker symbol", required = true, example = "AAPL") @PathVariable String symbol) {
        AssetResponse response = assetService.getAssetBySymbol(symbol);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh historical data", description = "Fetches the latest historical price data from AlphaVantage for the given asset symbol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical data refreshed successfully"),
            @ApiResponse(responseCode = "404", description = "Asset not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/symbol/{symbol}/refresh")
    public ResponseEntity<Void> refreshHistoricalData(
            @Parameter(description = "Ticker symbol", required = true, example = "AAPL") @PathVariable String symbol) {
        assetService.refreshHistoricalData(symbol);
        return ResponseEntity.ok().build();
    }
}
