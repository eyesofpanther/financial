package com.market.financial.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "Consolidado_Mensal")
public class MonthlyConsolidated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Consolidado")
    private Long id;

    @Column(name = "Date", nullable = false, length = 19)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Asset_ID", referencedColumnName = "ID_Asset", nullable = false)
    private Asset asset;

    @Column(name = "Stock", nullable = false)
    private BigDecimal stock;

    public MonthlyConsolidated() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public BigDecimal getStock() { return stock; }
    public void setStock(BigDecimal stock) { this.stock = stock; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyConsolidated that = (MonthlyConsolidated) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MonthlyConsolidated{" +
                "id=" + id +
                ", date=" + date +
                ", assetId=" + (asset != null ? asset.getId() : "null") +
                ", stock=" + stock +
                '}';
    }
}
