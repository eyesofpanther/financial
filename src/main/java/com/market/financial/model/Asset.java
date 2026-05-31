package com.market.financial.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Asset")
public class Asset {

    @Id
    @Column(name = "ID_Asset")
    private String id;

    @Column(name = "Description", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "Asset_Type_ID", referencedColumnName = "ID_Asset_Type", nullable = false)
    private AssetType assetType;

    public Asset() {
    }

    public Asset(String id, String description, AssetType assetType) {
        this.id = id;
        this.description = description;
        this.assetType = assetType;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", type=" + (assetType != null ? assetType.getDescription() : "null") +
                '}';
    }

}
