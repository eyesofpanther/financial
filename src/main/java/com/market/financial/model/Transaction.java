package com.market.financial.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "Transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Transaction")
    private Long id;

    @Column(name = "Date", nullable = false, length = 19)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Operation_ID", referencedColumnName = "ID_Operation")
    private OperationType operationType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_Asset", referencedColumnName = "ID_Asset")
    private Asset asset;

    @Column(name = "Stock", nullable = false)
    private BigDecimal stock;

    @Column(name = "Unit_Value")
    private BigDecimal unitValue;

    @Column(name = "Fee")
    private BigDecimal fee;

    @Column(name = "Memo", length = 50)
    private String memo;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "RefCompra")
    private Integer refCompra;

    @Column(name = "Date_Sales", length = 19)
    private LocalDate dateSales;

    public Transaction() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public OperationType getOperationType() { return operationType; }
    public void setOperationType(OperationType operationType) { this.operationType = operationType; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public BigDecimal getStock() { return stock; }
    public void setStock(BigDecimal stock) { this.stock = stock; }

    public BigDecimal getUnitValue() { return unitValue; }
    public void setUnitValue(BigDecimal unitValue) { this.unitValue = unitValue; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public Integer getActive() { return active; }
    public void setActive(Integer active) { this.active = active; }

    public Integer getRefCompra() { return refCompra; }
    public void setRefCompra(Integer refCompra) { this.refCompra = refCompra; }

    public LocalDate getDateSales() { return dateSales; }
    public void setDateSales(LocalDate dateSales) { this.dateSales = dateSales; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", date=" + date +
                ", assetId=" + (asset != null ? asset.getId() : "null") +
                ", stock=" + stock +
                ", unitValue=" + unitValue +
                '}';
    }
}
