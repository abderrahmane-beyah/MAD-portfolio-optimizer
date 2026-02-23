package com.port.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "historical_returns",
       uniqueConstraints = @UniqueConstraint(columnNames = {"asset_id", "date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double returnValue;

    @Column(nullable = false)
    private Double closePrice;

    @Column
    private Double openPrice;

    @Column
    private Double highPrice;

    @Column
    private Double lowPrice;

    @Column
    private Long volume;
}
