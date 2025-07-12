package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.UserRepository;
import com.programacao_web.rpg_market.repository.ProductRepository;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import com.programacao_web.rpg_market.service.DataInitializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class DataSeedController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DataInitializerService dataInitializerService;

    /**
     * Endpoint para mestres recriarem os dados de demonstração robustos
     */
    @PostMapping("/criar-dados-demonstracao")
    @PreAuthorize("hasRole('MESTRE') or hasRole('ADMIN')")
    public String createDemoData(RedirectAttributes redirectAttributes) {
        try {
            long existingUsers = userRepository.count();
            long existingProducts = productRepository.count();
            long existingTransactions = transactionRepository.count();
            
            // Forçar execução do DataInitializerService através do método específico
            dataInitializerService.createDemoData();

            long newUsers = userRepository.count();
            long newProducts = productRepository.count();
            long newTransactions = transactionRepository.count();

            redirectAttributes.addFlashAttribute("success", 
                String.format("✅ Dados de demonstração robustos criados com sucesso! " +
                "Usuários: %d (+%d), Produtos: %d (+%d), Transações: %d (+%d)",
                newUsers, newUsers - existingUsers,
                newProducts, newProducts - existingProducts,
                newTransactions, newTransactions - existingTransactions));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "❌ Erro ao criar dados de demonstração: " + e.getMessage());
        }

        return "redirect:/mestre/gestao-anuncios";
    }

    /**
     * Endpoint original simplificado para compatibilidade (obsoleto)
     */
    /**
     * Endpoint original simplificado para compatibilidade (obsoleto)
     */
    @PostMapping("/criar-dados-simples")
    @PreAuthorize("hasRole('MESTRE') or hasRole('ADMIN')")
    public String createSimpleDemoData(RedirectAttributes redirectAttributes) {
        try {
            // Buscar usuário admin
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin == null) {
                redirectAttributes.addFlashAttribute("error", "Usuário admin não encontrado!");
                return "redirect:/mestre/gestao-anuncios";
            }

            // Contar produtos existentes
            long existingProducts = productRepository.count();
            
            // Verificar se já existem produtos suficientes
            if (existingProducts >= 5) {
                redirectAttributes.addFlashAttribute("warning", 
                    "Já existem " + existingProducts + " produtos no sistema. " +
                    "Os dados de demonstração não foram criados para evitar duplicação.");
                return "redirect:/mestre/gestao-anuncios";
            }

            int produtosCriados = 0;

            // Anúncio 1: Espada Longa de Aço Valiriano (Venda Direta)
            if (!productRepository.existsByName("Espada Longa de Aço Valiriano")) {
                Product espada = new Product();
                espada.setName("Espada Longa de Aço Valiriano");
                espada.setDescription("Uma lâmina forjada nas profundezas das Montanhas da Perdição, reforçada com o lendário Aço Valiriano. Leve, incrivelmente afiada e resistente a qualquer magia sombria.");
                espada.setPrice(new BigDecimal("750.00"));
                espada.setType(ProductType.DIRECT_SALE);
                espada.setCategory(ProductCategory.ARMAS);
                espada.setStatus(ProductStatus.AVAILABLE);
                espada.setRarity(ItemRarity.MUITO_RARO);
                espada.setImageUrl("/img/items/espada_valiriana.png");
                espada.setCreatedAt(LocalDateTime.now());
                espada.setSeller(admin);
                productRepository.save(espada);
                produtosCriados++;
            }

            // Anúncio 2: Arco Élfico de Lunária (Leilão)
            if (!productRepository.existsByName("Arco Élfico de Galhos de Lunária")) {
                Product arco = new Product();
                arco.setName("Arco Élfico de Galhos de Lunária");
                arco.setDescription("Um arco graciosamente esculpido a partir de galhos da rara árvore Lunária, encontrada nas florestas encantadas de Eldoria. Incrivelmente preciso e silencioso.");
                arco.setPrice(new BigDecimal("300.00"));
                arco.setBuyNowPrice(new BigDecimal("600.00"));
                arco.setMinBidIncrement(new BigDecimal("20.00"));
                arco.setType(ProductType.AUCTION);
                arco.setAuctionEndDate(LocalDateTime.now().plusDays(5));
                arco.setCategory(ProductCategory.ARMAS);
                arco.setStatus(ProductStatus.AUCTION_ACTIVE);
                arco.setRarity(ItemRarity.RARO);
                arco.setImageUrl("/img/items/arco_elfico.png");
                arco.setCreatedAt(LocalDateTime.now());
                arco.setSeller(admin);
                productRepository.save(arco);
                produtosCriados++;
            }

            // Anúncio 3: Poção de Cura Maior (Venda Direta)
            if (!productRepository.existsByName("Poção de Cura Maior")) {
                Product pocao = new Product();
                pocao.setName("Poção de Cura Maior");
                pocao.setDescription("Uma poderosa concoção alquímica capaz de curar ferimentos graves em instantes. Essencial para qualquer aventureiro que se preze.");
                pocao.setPrice(new BigDecimal("150.00"));
                pocao.setType(ProductType.DIRECT_SALE);
                pocao.setCategory(ProductCategory.POCOES_ELIXIRES);
                pocao.setStatus(ProductStatus.AVAILABLE);
                pocao.setRarity(ItemRarity.INCOMUM);
                pocao.setImageUrl("/img/items/pocao_cura.png");
                pocao.setCreatedAt(LocalDateTime.now());
                pocao.setSeller(admin);
                productRepository.save(pocao);
                produtosCriados++;
            }

            // Anúncio 4: Mochila de Couro de Ogro Encantada (Venda Direta)
            if (!productRepository.existsByName("Mochila de Couro de Ogro Encantada")) {
                Product mochila = new Product();
                mochila.setName("Mochila de Couro de Ogro Encantada");
                mochila.setDescription("Uma mochila espaçosa feita de couro resistente de ogro, com um encantamento que permite carregar até o dobro do peso sem sentir o fardo.");
                mochila.setPrice(new BigDecimal("220.00"));
                mochila.setType(ProductType.DIRECT_SALE);
                mochila.setCategory(ProductCategory.DIVERSOS);
                mochila.setStatus(ProductStatus.AVAILABLE);
                mochila.setRarity(ItemRarity.INCOMUM);
                mochila.setImageUrl("/img/items/mochila_ogro.png");
                mochila.setCreatedAt(LocalDateTime.now());
                mochila.setSeller(admin);
                productRepository.save(mochila);
                produtosCriados++;
            }

            // Anúncio 5: Amuleto da Proteção Menor (Venda Direta)
            if (!productRepository.existsByName("Amuleto da Proteção Menor")) {
                Product amuleto = new Product();
                amuleto.setName("Amuleto da Proteção Menor");
                amuleto.setDescription("Um amuleto de prata adornado com uma pequena pedra de jaspe. Oferece uma leve proteção contra energias negativas e pequenos golpes.");
                amuleto.setPrice(new BigDecimal("60.00"));
                amuleto.setType(ProductType.DIRECT_SALE);
                amuleto.setCategory(ProductCategory.JOIAS_ARTEFATOS);
                amuleto.setStatus(ProductStatus.AVAILABLE);
                amuleto.setRarity(ItemRarity.COMUM);
                amuleto.setImageUrl("/img/items/amuleto_protecao.png");
                amuleto.setCreatedAt(LocalDateTime.now());
                amuleto.setSeller(admin);
                productRepository.save(amuleto);
                produtosCriados++;
            }

            if (produtosCriados > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    "✅ Dados de demonstração criados com sucesso! " + 
                    produtosCriados + " produtos adicionados ao mercado.");
            } else {
                redirectAttributes.addFlashAttribute("info", 
                    "ℹ️ Todos os produtos de demonstração já existem no sistema.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "❌ Erro ao criar dados de demonstração: " + e.getMessage());
        }

        return "redirect:/mestre/gestao-anuncios";
    }
}
