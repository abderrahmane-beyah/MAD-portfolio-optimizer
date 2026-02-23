package com.port.demo.service;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.port.demo.entity.*;
import com.port.demo.exception.OptimizationException;
import com.port.demo.exception.ResourceNotFoundException;
import com.port.demo.repository.HistoricalReturnRepository;
import com.port.demo.repository.OptimizationResultRepository;
import com.port.demo.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimizationService {

    private final PortfolioRepository portfolioRepository;
    private final HistoricalReturnRepository historicalReturnRepository;
    private final OptimizationResultRepository optimizationResultRepository;

    static {
        Loader.loadNativeLibraries();
    }

    @Transactional
    public OptimizationResult optimizePortfolio(Long portfolioId, Double alphaOverride, int lookbackDays) {
        log.info("Starting MAD optimization for portfolio: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findByIdWithAssets(portfolioId);
        if (portfolio == null) {
            throw new ResourceNotFoundException("Portfolio not found: " + portfolioId);
        }

        double alpha = alphaOverride != null ? alphaOverride : portfolio.getDefaultAlpha();

        try {
            // Fetch historical returns data
            List<PortfolioAsset> portfolioAssets = portfolio.getPortfolioAssets();
            if (portfolioAssets.isEmpty()) {
                throw new OptimizationException("Portfolio has no assets");
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(lookbackDays);

            List<Long> assetIds = portfolioAssets.stream()
                    .map(pa -> pa.getAsset().getId())
                    .collect(Collectors.toList());

            List<HistoricalReturn> allReturns = historicalReturnRepository
                    .findByAssetIdsAndDateRange(assetIds, startDate, endDate);

            // Organize data by asset and time period
            Map<Long, List<HistoricalReturn>> returnsByAsset = allReturns.stream()
                    .collect(Collectors.groupingBy(hr -> hr.getAsset().getId()));

            // Get common dates across all assets
            Set<LocalDate> commonDates = returnsByAsset.values().stream()
                    .map(returns -> returns.stream().map(HistoricalReturn::getDate).collect(Collectors.toSet()))
                    .reduce((set1, set2) -> {
                        set1.retainAll(set2);
                        return set1;
                    })
                    .orElse(Collections.emptySet());

            if (commonDates.isEmpty()) {
                throw new OptimizationException("No common dates found for historical returns");
            }

            List<LocalDate> sortedDates = new ArrayList<>(commonDates);
            sortedDates.sort(LocalDate::compareTo);

            // Build returns matrix R[t][j] and calculate expected returns E[R_j]
            int T = sortedDates.size();
            int N = portfolioAssets.size();
            double[][] returnsMatrix = new double[T][N];
            double[] expectedReturns = new double[N];
            Map<Integer, PortfolioAsset> indexToAsset = new HashMap<>();

            for (int j = 0; j < N; j++) {
                PortfolioAsset pa = portfolioAssets.get(j);
                indexToAsset.put(j, pa);
                List<HistoricalReturn> assetReturns = returnsByAsset.get(pa.getAsset().getId());

                Map<LocalDate, Double> returnMap = assetReturns.stream()
                        .collect(Collectors.toMap(HistoricalReturn::getDate, HistoricalReturn::getReturnValue));

                double sum = 0.0;
                for (int t = 0; t < T; t++) {
                    double ret = returnMap.getOrDefault(sortedDates.get(t), 0.0);
                    returnsMatrix[t][j] = ret;
                    sum += ret;
                }
                expectedReturns[j] = sum / T;
            }

            // Solve MAD LP using OR-Tools
            MPSolver solver = MPSolver.createSolver("GLOP");
            if (solver == null) {
                throw new OptimizationException("Could not create solver");
            }

            // Decision variables: x[j] = weight of asset j
            MPVariable[] x = new MPVariable[N];
            for (int j = 0; j < N; j++) {
                PortfolioAsset pa = indexToAsset.get(j);
                x[j] = solver.makeNumVar(pa.getMinWeight(), pa.getMaxWeight(), "x_" + j);
            }

            // Auxiliary variables: y[t] for absolute deviations
            MPVariable[] y = new MPVariable[T];
            for (int t = 0; t < T; t++) {
                y[t] = solver.makeNumVar(0, Double.POSITIVE_INFINITY, "y_" + t);
            }

            // Budget constraint: sum(x[j]) = 1
            MPConstraint budgetConstraint = solver.makeConstraint(1.0, 1.0, "budget");
            for (int j = 0; j < N; j++) {
                budgetConstraint.setCoefficient(x[j], 1.0);
            }

            // Linearization constraints for absolute values
            // For each time period t:
            // y[t] >= sum_j (R[t][j] - E[R_j]) * x[j]
            // y[t] >= -sum_j (R[t][j] - E[R_j]) * x[j]
            for (int t = 0; t < T; t++) {
                MPConstraint constraint1 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0, "abs_pos_" + t);
                MPConstraint constraint2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0, "abs_neg_" + t);

                constraint1.setCoefficient(y[t], 1.0);
                constraint2.setCoefficient(y[t], 1.0);

                for (int j = 0; j < N; j++) {
                    double deviation = returnsMatrix[t][j] - expectedReturns[j];
                    constraint1.setCoefficient(x[j], -deviation);  // y[t] - deviation*x[j] >= 0
                    constraint2.setCoefficient(x[j], deviation);   // y[t] + deviation*x[j] >= 0
                }
            }

            // Objective: Maximize sum(E[R_j] * x[j]) - alpha * sum(y[t])
            MPObjective objective = solver.objective();
            for (int j = 0; j < N; j++) {
                objective.setCoefficient(x[j], expectedReturns[j]);
            }
            for (int t = 0; t < T; t++) {
                objective.setCoefficient(y[t], -alpha / T);  // Normalize by T
            }
            objective.setMaximization();

            // Solve
            MPSolver.ResultStatus resultStatus = solver.solve();

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
                log.info("Optimization successful for portfolio: {}", portfolioId);

                // Extract solution
                Map<String, Double> weights = new HashMap<>();
                double expectedReturn = 0.0;
                double riskMad = 0.0;

                for (int j = 0; j < N; j++) {
                    double weight = x[j].solutionValue();
                    if (weight > 1e-6) {  // Only include non-zero weights
                        String symbol = indexToAsset.get(j).getAsset().getSymbol();
                        weights.put(symbol, weight);
                        expectedReturn += weight * expectedReturns[j];
                    }
                }

                for (int t = 0; t < T; t++) {
                    riskMad += y[t].solutionValue();
                }
                riskMad /= T;  // Average MAD

                double objectiveValue = objective.value();

                // Save result
                OptimizationResult result = OptimizationResult.builder()
                        .portfolio(portfolio)
                        .alphaUsed(alpha)
                        .expectedReturn(expectedReturn)
                        .riskMad(riskMad)
                        .objectiveValue(objectiveValue)
                        .weights(weights)
                        .status(OptimizationResult.OptimizationStatus.SUCCESS)
                        .build();

                return optimizationResultRepository.save(result);

            } else {
                log.error("Optimization failed with status: {}", resultStatus);
                OptimizationResult result = OptimizationResult.builder()
                        .portfolio(portfolio)
                        .alphaUsed(alpha)
                        .expectedReturn(0.0)
                        .riskMad(0.0)
                        .objectiveValue(0.0)
                        .weights(Collections.emptyMap())
                        .status(OptimizationResult.OptimizationStatus.INFEASIBLE)
                        .errorMessage("Solver status: " + resultStatus)
                        .build();

                return optimizationResultRepository.save(result);
            }

        } catch (Exception e) {
            log.error("Error during optimization", e);
            OptimizationResult result = OptimizationResult.builder()
                    .portfolio(portfolio)
                    .alphaUsed(alpha)
                    .expectedReturn(0.0)
                    .riskMad(0.0)
                    .objectiveValue(0.0)
                    .weights(Collections.emptyMap())
                    .status(OptimizationResult.OptimizationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();

            return optimizationResultRepository.save(result);
        }
    }
}
