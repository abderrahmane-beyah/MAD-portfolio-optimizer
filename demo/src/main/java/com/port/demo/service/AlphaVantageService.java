package com.port.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.port.demo.config.AlphaVantageConfig;
import com.port.demo.entity.Asset;
import com.port.demo.entity.HistoricalReturn;
import com.port.demo.exception.OptimizationException;
import com.port.demo.repository.HistoricalReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlphaVantageService {

    private final AlphaVantageConfig config;
    private final HistoricalReturnRepository historicalReturnRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void fetchAndStoreHistoricalData(Asset asset) {
        log.info("Fetching historical data for symbol: {}", asset.getSymbol());

        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s",
                config.getBaseUrl(), asset.getSymbol(), config.getKey());

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Check for error message
            if (root.has("Error Message")) {
                throw new OptimizationException("AlphaVantage API Error: " + root.get("Error Message").asText());
            }

            // Check for rate limit
            if (root.has("Note")) {
                throw new OptimizationException("AlphaVantage API rate limit exceeded");
            }

            JsonNode timeSeries = root.get("Time Series (Daily)");
            if (timeSeries == null) {
                throw new OptimizationException("No time series data found for symbol: " + asset.getSymbol());
            }

            List<HistoricalReturn> returns = new ArrayList<>();
            List<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
            fields.forEachRemaining(entries::add);

            // Sort by date ascending
            entries.sort(Map.Entry.comparingByKey());

            Double previousClose = null;
            for (Map.Entry<String, JsonNode> entry : entries) {
                LocalDate date = LocalDate.parse(entry.getKey(), DateTimeFormatter.ISO_DATE);
                JsonNode dayData = entry.getValue();

                Double open = dayData.get("1. open").asDouble();
                Double high = dayData.get("2. high").asDouble();
                Double low = dayData.get("3. low").asDouble();
                Double close = dayData.get("4. close").asDouble();
                Long volume = dayData.get("5. volume").asLong();

                // Calculate return
                Double returnValue = 0.0;
                if (previousClose != null && previousClose != 0) {
                    returnValue = (close - previousClose) / previousClose;
                }

                HistoricalReturn historicalReturn = HistoricalReturn.builder()
                        .asset(asset)
                        .date(date)
                        .returnValue(returnValue)
                        .closePrice(close)
                        .openPrice(open)
                        .highPrice(high)
                        .lowPrice(low)
                        .volume(volume)
                        .build();

                returns.add(historicalReturn);
                previousClose = close;
            }

            historicalReturnRepository.saveAll(returns);
            log.info("Stored {} historical returns for symbol: {}", returns.size(), asset.getSymbol());

        } catch (Exception e) {
            log.error("Error fetching data from AlphaVantage for symbol: {}", asset.getSymbol(), e);
            throw new OptimizationException("Failed to fetch historical data: " + e.getMessage(), e);
        }
    }

    public List<HistoricalReturn> getHistoricalReturns(Long assetId, int lookbackDays) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(lookbackDays);
        return historicalReturnRepository.findByAssetIdAndDateBetweenOrderByDateAsc(assetId, startDate, endDate);
    }
}
