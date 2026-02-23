package com.port.demo.repository;

import com.port.demo.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findBySymbol(String symbol);
    List<Asset> findBySymbolIn(List<String> symbols);
    boolean existsBySymbol(String symbol);
}
