package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioConsolidadoDTO(
    LocalDate data,
    String ativo,
    BigDecimal quantidadeCotas,
    BigDecimal precoNaData,
    BigDecimal valorPatrimonialNaData
) {}
