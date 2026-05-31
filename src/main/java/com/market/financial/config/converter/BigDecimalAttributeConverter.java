package com.market.financial.config.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

@Converter(autoApply = true)
public class BigDecimalAttributeConverter implements AttributeConverter<BigDecimal, Object> {

    @Override
    public Object convertToDatabaseColumn(BigDecimal attribute) {
        return attribute;
    }

    @Override
    public BigDecimal convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Se o SQLite devolver uma String (vazia ou com espaços), trata defensivamente
        if (dbData instanceof String str) {
            if (str.trim().isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(str.trim());
            } catch (NumberFormatException e) {
                System.err.println("Erro ao converter String para BigDecimal: [" + str + "]");
                return null;
            }
        }
        
        // Se for um tipo numérico nativo (Double, Integer, Long), converte direto
        if (dbData instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        return null;
    }
}
