package com.port.demo.repository;

import com.port.demo.entity.OptimizationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimizationResultRepository extends JpaRepository<OptimizationResult, Long> {
    List<OptimizationResult> findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);
}
