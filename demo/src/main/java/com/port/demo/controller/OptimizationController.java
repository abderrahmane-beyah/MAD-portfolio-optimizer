package com.port.demo.controller;

import com.port.demo.dto.ErrorResponse;
import com.port.demo.dto.OptimizationRequest;
import com.port.demo.dto.OptimizationResponse;
import com.port.demo.entity.OptimizationResult;
import com.port.demo.service.OptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/optimization")
@RequiredArgsConstructor
@Tag(name = "Optimization", description = "Portfolio optimization using MAD linearization")
public class OptimizationController {

    private final OptimizationService optimizationService;

    @Operation(summary = "Optimize a portfolio",
            description = "Runs MAD (Mean Absolute Deviation) linear optimization on a portfolio. "
                    + "Uses historical return data to compute optimal asset weights that maximize expected return "
                    + "for a given risk tolerance (alpha). The lookback period controls how many days of historical data are used.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Optimization completed successfully",
                    content = @Content(schema = @Schema(implementation = OptimizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Optimization failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OptimizationResponse> optimizePortfolio(@Valid @RequestBody OptimizationRequest request) {
        OptimizationResult result = optimizationService.optimizePortfolio(
                request.getPortfolioId(),
                request.getAlphaOverride(),
                request.getLookbackDays()
        );

        OptimizationResponse response = OptimizationResponse.builder()
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

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get optimization result", description = "Retrieves a specific optimization result by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Optimization result found",
                    content = @Content(schema = @Schema(implementation = OptimizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Optimization result not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OptimizationResponse> getOptimizationResult(
            @Parameter(description = "Optimization result ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
