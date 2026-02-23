package com.port.demo.repository;

import com.port.demo.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.portfolioAssets WHERE p.id = :id")
    Portfolio findByIdWithAssets(Long id);
}
