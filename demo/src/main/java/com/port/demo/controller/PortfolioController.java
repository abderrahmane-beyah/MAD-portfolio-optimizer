package com.port.demo.controller;

import com.port.demo.dto.CreatePortfolioRequest;
import com.port.demo.dto.ErrorResponse;
import com.port.demo.dto.OptimizationResponse;
import com.port.demo.dto.PortfolioResponse;
import com.port.demo.service.PortfolioService;
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
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolios", description = "Portfolio management operations")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "Create a new portfolio", description = "Creates a portfolio with asset constraints (min/max weights) for a given user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Portfolio created successfully",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or asset not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        PortfolioResponse response = portfolioService.createPortfolio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get portfolio by ID", description = "Retrieves a portfolio with its asset constraints by unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portfolio found",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolioById(
            @Parameter(description = "Portfolio ID", required = true) @PathVariable Long id) {
        PortfolioResponse response = portfolioService.getPortfolioById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get portfolios by user", description = "Retrieves all portfolios belonging to a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of portfolios",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PortfolioResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PortfolioResponse>> getPortfoliosByUserId(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        List<PortfolioResponse> portfolios = portfolioService.getPortfoliosByUserId(userId);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Delete a portfolio", description = "Permanently deletes a portfolio and its associated data")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Portfolio deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "Portfolio ID", required = true) @PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get optimization history", description = "Retrieves all past optimization results for a given portfolio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of optimization results",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OptimizationResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{portfolioId}/optimizations")
    public ResponseEntity<List<OptimizationResponse>> getOptimizationHistory(
            @Parameter(description = "Portfolio ID", required = true) @PathVariable Long portfolioId) {
        List<OptimizationResponse> history = portfolioService.getOptimizationHistory(portfolioId);
        return ResponseEntity.ok(history);
    }
}
