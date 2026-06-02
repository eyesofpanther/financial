package com.market.financial.config;

// import com.market.financial.model.Asset;
import com.market.financial.repository.AssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
// import java.util.List;

@Component
public class DatabaseLoader implements CommandLineRunner {

    // // Injeção de dependência automática pelo Spring
    public DatabaseLoader(AssetRepository assetRepository) {
    }

    @Override
    // public void run(String... args) throws Exception {
    //     System.out.println("\n--- INICIANDO SELECT NA TABELA ASSET ---");
        
    //     List<Asset> assets = assetRepository.findAll();
        
    //     if (assets.isEmpty()) {
    //         System.out.println("Nenhum registro encontrado na tabela Asset.");
    //     } else {
    //         assets.forEach(System.out::println);
    //     }
        
    //     System.out.println("----------------------------------------\n");
    // }

    public void run(String... args) throws Exception {
        // Método limpo para evitar consultas desnecessárias no log de inicialização
    }
}
