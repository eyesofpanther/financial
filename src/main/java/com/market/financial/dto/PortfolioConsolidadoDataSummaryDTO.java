package com.market.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PortfolioConsolidadoDataSummaryDTO(
    LocalDate data,
    BigDecimal valorTotalPatrimonial,
    List<PortfolioConsolidadoDTO> ativos
) {}
