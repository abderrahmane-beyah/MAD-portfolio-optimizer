package com.port.demo.repository;

import com.port.demo.entity.HistoricalReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistoricalReturnRepository extends JpaRepository<HistoricalReturn, Long> {

    List<HistoricalReturn> findByAssetIdAndDateBetweenOrderByDateAsc(
            Long assetId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT hr FROM HistoricalReturn hr WHERE hr.asset.id IN :assetIds " +
           "AND hr.date BETWEEN :startDate AND :endDate ORDER BY hr.date ASC")
    List<HistoricalReturn> findByAssetIdsAndDateRange(
            List<Long> assetIds, LocalDate startDate, LocalDate endDate);

    @Query("SELECT hr FROM HistoricalReturn hr WHERE hr.asset.symbol = :symbol " +
           "ORDER BY hr.date DESC")
    List<HistoricalReturn> findLatestBySymbol(String symbol);

    Long countByAssetId(Long assetId);
}
