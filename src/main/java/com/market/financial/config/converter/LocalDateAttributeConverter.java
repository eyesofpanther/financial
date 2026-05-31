package com.market.financial.config.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String convertToDatabaseColumn(LocalDate locDate) {
        return (locDate == null ? null : locDate.format(FORMATTER));
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        // Trata nulos, vazios ou registros corrompidos no SQLite
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        // Remove espaços extras que o tipo TEXT(19) possa ter gerado
        String cleanedData = dbData.trim();
        
        // Se a data vier maior (ex: "2024-05-24 00:00:00"), captura apenas os 10 primeiros caracteres (yyyy-MM-dd)
        if (cleanedData.length() > 10) {
            cleanedData = cleanedData.substring(0, 10);
        }
        
        try {
            return LocalDate.parse(cleanedData, FORMATTER);
        } catch (Exception e) {
            // Log do erro se necessário, retorna null para não derrubar o GET da API
            System.err.println("Falha ao converter o valor do banco para LocalDate: [" + dbData + "]");
            return null; 
        }
    }
}
