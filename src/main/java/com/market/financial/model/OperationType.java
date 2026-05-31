package com.market.financial.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Operation_Type")
public class OperationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Operation")
    private Integer idOperation;

    @Column(name = "Description")
    @Size(max = 20, message = "A descrição deve ter no máximo 20 caracteres")
    private String description;

    @Column(name = "IO")
    @Min(value = 0, message = "O campo IO deve ser 0 ou 1")
    @Max(value = 1, message = "O campo IO deve ser 0 ou 1")
    private Integer io;

    // Construtores
    public OperationType() {}

    public OperationType(Integer idOperation, String description, Integer io) {
        this.idOperation = idOperation;
        this.description = description;
        this.io = io;
    }

    // Getters e Setters
    public Integer getIdOperation() { return idOperation; }
    public void setIdOperation(Integer idOperation) { this.idOperation = idOperation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getIo() { return io; }
    public void setIo(Integer io) { this.io = io; }

    @Override
    public String toString() {
        return "OperationType [idOperation=" + idOperation + ", description=" + description + ", io=" + io + "]";
    }

    
}
