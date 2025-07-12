package com.programacao_web.rpg_market.config;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.UserRole;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductType;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.ItemRarity;
import com.programacao_web.rpg_market.model.TransactionStatus;
import com.programacao_web.rpg_market.repository.UserRepository;
import com.programacao_web.rpg_market.repository.ProductRepository;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean    CommandLineRunner init(UserRepository userRepository, 
                          ProductRepository productRepository,
                          TransactionRepository transactionRepository,
                          PasswordEncoder passwordEncoder) {return args -> {
            System.out.println("=== Inicializando dados do MongoDB ===");
            long userCount = userRepository.count();
            long productCount = productRepository.count();
            System.out.println("Usuários existentes: " + userCount);
            System.out.println("Produtos existentes: " + productCount);
            
            // Vamos listar os usuários existentes para debug
            if (userCount > 0) {
                System.out.println("=== Usuários existentes no banco ===");
                userRepository.findAll().forEach(user -> {
                    System.out.println("Username: " + user.getUsername() + ", Email: " + user.getEmail() + ", Role: " + user.getRole());
                });
            }            // Verifica se o usuário admin existe, independente da quantidade de usuários
            if (!userRepository.existsByUsername("admin")) {
                System.out.println("Criando usuário admin...");
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@rpgmarket.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setCharacterClass("Mago");
                admin.setLevel(99);
                admin.setExperience(1000);
                admin.setGoldCoins(new BigDecimal("10000"));
                admin.setRole(UserRole.ROLE_MESTRE);
                userRepository.save(admin);
                
                System.out.println("✅ Usuário admin criado com sucesso!");
            } else {
                System.out.println("Usuário admin já existe no banco.");
            }
            
            if (userCount == 0) {
                System.out.println("Criando usuários iniciais adicionais...");
                
                // Create test buyer user
                User buyer = new User();
                buyer.setUsername("testuser");
                buyer.setEmail("test@rpgmarket.com");
                buyer.setPassword(passwordEncoder.encode("password"));
                buyer.setCharacterClass("Guerreiro");
                buyer.setLevel(10);
                buyer.setExperience(500);                buyer.setGoldCoins(new BigDecimal("1000"));
                buyer.setRole(UserRole.ROLE_AVENTUREIRO);
                userRepository.save(buyer);                System.out.println("✅ Usuário testuser criado:");
                System.out.println("   - Teste: username='testuser', password='password'");
            } else {
                System.out.println("Outros usuários já existem, pulando criação de usuário teste...");
            }
            
            // Verificar se o usuário admin foi salvo corretamente
            User savedAdmin = userRepository.findByUsername("admin").orElse(null);
            if (savedAdmin != null) {
                System.out.println("✅ Admin confirmado no banco: " + savedAdmin.getUsername());
                System.out.println("   Password hash: " + savedAdmin.getPassword().substring(0, Math.min(10, savedAdmin.getPassword().length())) + "...");
                System.out.println("   Role: " + savedAdmin.getRole());
            } else {
                System.out.println("❌ Erro: Admin não encontrado no banco!");
            }
            
            // Criar produtos de demonstração se houver menos de 5 produtos
            if (productCount < 5) {
                System.out.println("Criando produtos de demonstração (existentes: " + productCount + ")...");
                User admin = userRepository.findByUsername("admin").orElse(null);

                if (admin != null) {
                    // Anúncio 1: Espada Longa de Aço Valiriano (Venda Direta)
                    Product espada = new Product();
                    espada.setName("Espada Longa de Aço Valiriano");
                    espada.setDescription("Uma lâmina forjada nas profundezas das Montanhas da Perdição, reforçada com o lendário Aço Valiriano. Leve, incrivelmente afiada e resistente a qualquer magia sombria.");
                    espada.setPrice(new BigDecimal("750.00")); // Preço para venda direta
                    espada.setType(ProductType.DIRECT_SALE); // Tipo: Venda Direta
                    espada.setCategory(ProductCategory.ARMAS); // Categoria: Armas
                    espada.setStatus(ProductStatus.AVAILABLE); // Status: Disponível
                    espada.setRarity(ItemRarity.MUITO_RARO); // Raridade: Épico
                    espada.setImageUrl("/img/items/espada_valiriana.png"); // Caminho da imagem
                    espada.setSeller(admin); // Vendedor
                    productRepository.save(espada);

                    // Anúncio 2: Arco Élfico de Lunária (Leilão)
                    Product arco = new Product();
                    arco.setName("Arco Élfico de Galhos de Lunária");
                    arco.setDescription("Um arco graciosamente esculpido a partir de galhos da rara árvore Lunária, encontrada nas florestas encantadas de Eldoria. Incrivelmente preciso e silencioso.");
                    arco.setPrice(new BigDecimal("300.00")); // Lance inicial do leilão
                    arco.setBuyNowPrice(new BigDecimal("600.00")); // Preço de "Comprar Agora"
                    arco.setMinBidIncrement(new BigDecimal("20.00")); // Incremento mínimo do lance
                    arco.setType(ProductType.AUCTION); // Tipo: Leilão
                    arco.setAuctionEndDate(LocalDateTime.now().plusDays(5)); // Data de fim do leilão
                    arco.setCategory(ProductCategory.ARMAS); // Categoria: Armas
                    arco.setStatus(ProductStatus.AUCTION_ACTIVE); // Status: Leilão ativo
                    arco.setRarity(ItemRarity.RARO); // Raridade: Raro
                    arco.setImageUrl("/img/items/arco_elfico.png");
                    arco.setSeller(admin);
                    productRepository.save(arco);

                    // Anúncio 3: Poção de Cura Maior (Venda Direta)
                    Product pocao = new Product();
                    pocao.setName("Poção de Cura Maior");
                    pocao.setDescription("Uma poderosa concoção alquímica capaz de curar ferimentos graves em instantes. Essencial para qualquer aventureiro que se preze.");
                    pocao.setPrice(new BigDecimal("150.00"));
                    pocao.setType(ProductType.DIRECT_SALE); // Tipo: Venda Direta
                    pocao.setCategory(ProductCategory.POCOES_ELIXIRES); // Categoria: Poções e Elixires
                    pocao.setStatus(ProductStatus.AVAILABLE); // Status: Disponível
                    pocao.setRarity(ItemRarity.INCOMUM); // Raridade: Incomum
                    pocao.setImageUrl("/img/items/pocao_cura.png");
                    pocao.setSeller(admin);
                    productRepository.save(pocao);
                    
                    // Anúncio 4: Mochila de Couro de Ogro Encantada (Venda Direta)
                    Product mochila = new Product();
                    mochila.setName("Mochila de Couro de Ogro Encantada");
                    mochila.setDescription("Uma mochila espaçosa feita de couro resistente de ogro, com um encantamento que permite carregar até o dobro do peso sem sentir o fardo.");
                    mochila.setPrice(new BigDecimal("220.00"));
                    mochila.setType(ProductType.DIRECT_SALE); // Tipo: Venda Direta
                    mochila.setCategory(ProductCategory.DIVERSOS); // Categoria: Itens Diversos
                    mochila.setStatus(ProductStatus.AVAILABLE); // Status: Disponível
                    mochila.setRarity(ItemRarity.INCOMUM); // Raridade: Incomum
                    mochila.setImageUrl("/img/items/mochila_ogro.png");
                    mochila.setSeller(admin);
                    productRepository.save(mochila);

                    // Anúncio 5: Amuleto da Proteção Menor (Venda Direta)
                    Product amuleto = new Product();
                    amuleto.setName("Amuleto da Proteção Menor");
                    amuleto.setDescription("Um amuleto de prata adornado com uma pequena pedra de jaspe. Oferece uma leve proteção contra energias negativas e pequenos golpes.");
                    amuleto.setPrice(new BigDecimal("60.00"));
                    amuleto.setType(ProductType.DIRECT_SALE); // Tipo: Venda Direta
                    amuleto.setCategory(ProductCategory.JOIAS_ARTEFATOS); // Categoria: Jóias e Artefatos
                    amuleto.setStatus(ProductStatus.AVAILABLE); // Status: Disponível
                    amuleto.setRarity(ItemRarity.COMUM); // Raridade: Comum
                    amuleto.setImageUrl("/img/items/amuleto_protecao.png");
                    amuleto.setSeller(admin);
                    productRepository.save(amuleto);
                      System.out.println("✅ Produtos de exemplo criados com sucesso!");                    // Criar algumas transações de exemplo se não existirem
                    if (transactionRepository.count() == 0) {
                        System.out.println("Criando transações de exemplo...");
                        
                        // Buscar alguns usuários para as transações
                        User aventureiro1 = userRepository.findByUsername("jogador1").orElse(null);
                        User aventureiro2 = userRepository.findByUsername("aventureiro").orElse(null);
                        
                        // Buscar produtos existentes
                        List<Product> produtos = productRepository.findAll();
                        
                        if (aventureiro1 != null && !produtos.isEmpty()) {
                            Product produto1 = produtos.get(0);
                            Transaction t1 = new Transaction();
                            t1.setBuyer(aventureiro1);
                            t1.setProduct(produto1);
                            t1.setAmount(produto1.getPrice());
                            t1.setStatus(TransactionStatus.COMPLETED);
                            t1.setCreatedAt(LocalDateTime.now().minusDays(1));
                            transactionRepository.save(t1);
                        }
                        
                        if (aventureiro2 != null && produtos.size() > 1) {
                            Product produto2 = produtos.get(1);
                            Transaction t2 = new Transaction();
                            t2.setBuyer(aventureiro2);
                            t2.setProduct(produto2);
                            t2.setAmount(produto2.getPrice());
                            t2.setStatus(TransactionStatus.COMPLETED);
                            t2.setCreatedAt(LocalDateTime.now().minusHours(6));
                            transactionRepository.save(t2);
                        }
                        
                        System.out.println("✅ Transações de exemplo criadas!");
                    }

                } else {
                    System.out.println("❌ Admin não encontrado para criar produtos!");
                }
            } else {
                 System.out.println("Produtos suficientes já existem ("+productCount+"), pulando criação de dados de exemplo.");
            }
            
            System.out.println("=== Inicialização concluída ===");
        };
    }
}