package com.market.financial.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Price")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Price")
    private Long idPrice;

    @Column(name = "Date", length = 19)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Asset_ID", referencedColumnName = "ID_Asset")
    private Asset asset;

    @Column(name = "Price")
    private BigDecimal price;

    // Getters e Setters (Mantidos iguais)
    public Long getIdPrice() { return idPrice; }
    public void setIdPrice(Long idPrice) { this.idPrice = idPrice; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    // EQUALS E HASHCODE baseados na identidade de negócio (Data + Ativo)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price other = (Price) o;
        return Objects.equals(date, other.date) && 
               Objects.equals(asset != null ? asset.getId() : null, 
                              other.asset != null ? other.asset.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, asset != null ? asset.getId() : null);
    }

    // TOSTRING seguro contra loops infinitos (StackOverflow)
    @Override
    public String toString() {
        return "Price{" +
                "idPrice=" + idPrice +
                ", date=" + date +
                ", assetId=" + (asset != null ? asset.getId() : "null") +
                ", price=" + price +
                '}';
    }
}
