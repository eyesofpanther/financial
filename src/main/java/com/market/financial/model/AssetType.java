package com.market.financial.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Asset_Type")
public class AssetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Mantido! O AUTOINCREMENT do banco exige isso.
    @Column(name = "ID_Asset_Type")
    private Integer id; // Alterado para Integer para casar perfeitamente com o INTEGER do SQLite

    @Column(name = "Description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "assetType")
    @JsonIgnore
    private List<Asset> assets;

    public AssetType() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Asset> getAssets() { return assets; }
    public void setAssets(List<Asset> assets) { this.assets = assets; }

    @Override
    public String toString() {
        return "AssetType{" + "id=" + id + ", description='" + description + '\'' + '}';
    }
}
