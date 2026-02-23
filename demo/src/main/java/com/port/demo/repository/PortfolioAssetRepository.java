package com.port.demo.repository;

import com.port.demo.entity.PortfolioAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset, Long> {
    List<PortfolioAsset> findByPortfolioId(Long portfolioId);
}
