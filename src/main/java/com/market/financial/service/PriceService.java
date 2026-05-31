package com.market.financial.service;

import com.market.financial.dto.PriceRequestDTO;
import com.market.financial.dto.PriceResponseDTO;
import com.market.financial.infra.exception.ResourceNotFoundException;
import com.market.financial.model.Asset;
import com.market.financial.model.Price;
import com.market.financial.repository.AssetRepository;
import com.market.financial.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    private final PriceRepository priceRepository;
    private final AssetRepository assetRepository; // Necessário para buscar o Asset pelo ID String

    public PriceService(PriceRepository priceRepository, AssetRepository assetRepository) {
        this.priceRepository = priceRepository;
        this.assetRepository = assetRepository;
    }

    public List<PriceResponseDTO> findAll() {
        return priceRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public Optional<PriceResponseDTO> findById(Long id) {
        return priceRepository.findById(id).map(this::convertToResponseDTO);
    }

    public PriceResponseDTO save(PriceRequestDTO request) {
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new RuntimeException("Asset não encontrado: " + request.assetId()));

        Price price = new Price();
        price.setDate(request.date());
        price.setAsset(asset);
        price.setPrice(request.price());

        Price savedPrice = priceRepository.save(price);
        return convertToResponseDTO(savedPrice);
    }

    public PriceResponseDTO update(Long id, PriceRequestDTO request) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado ID: " + id));

        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new RuntimeException("Asset não encontrado: " + request.assetId()));

        price.setDate(request.date());
        price.setAsset(asset);
        price.setPrice(request.price());

        Price updatedPrice = priceRepository.save(price);
        return convertToResponseDTO(updatedPrice);
    }

    public void delete(Long id) {
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preço não encontrado ID: " + id));
        priceRepository.delete(price);
    }

    // Método auxiliar de mapeamento
    private PriceResponseDTO convertToResponseDTO(Price price) {
        return new PriceResponseDTO(
                price.getIdPrice(),
                price.getDate(),
                price.getAsset() != null ? price.getAsset().getId() : null,
                price.getPrice());
    }

    public List<PriceResponseDTO> findByAssetId(String assetId) {
        return priceRepository.findByAssetId(assetId).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public PriceResponseDTO findByAssetIdAndDate(String assetId, LocalDate date) {
        return priceRepository.findByAssetIdAndDate(assetId, date)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preço não encontrado para o ativo " + assetId + " na data " + date));
    }

    public List<com.market.financial.dto.PriceResponseDTO> findByDate(java.time.LocalDate date) {
        List<com.market.financial.model.Price> prices = priceRepository.findByDate(date);
        if (prices.isEmpty()) {
            throw new com.market.financial.infra.exception.ResourceNotFoundException(
                    "Não existe cotação para a data " + date + ". Verifique se foi um dia útil ou fim de semana.");
        }
        return prices.stream().map(this::convertToResponseDTO).toList();
    }

}
