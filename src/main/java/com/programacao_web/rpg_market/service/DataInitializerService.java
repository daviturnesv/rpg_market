package com.programacao_web.rpg_market.service;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializerService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Só executa automaticamente na inicialização se não há dados
        if (args != null && args.length == 0) {
            // Execução automática na inicialização - só cria se não há dados
            if (userRepository.count() > 5) {
                log.info("Dados já existem em quantidade suficiente, pulando inicialização automática");
                return;
            }
        }
        // Se chamado programaticamente (args null), sempre executa

        createDemoData();
    }

    /**
     * Método público para criação de dados de demonstração que pode ser chamado
     * programaticamente pelo DataSeedController
     */
    @Transactional
    public void createDemoData() throws Exception {
        log.info("Iniciando criação de dados de demonstração robustos...");

        // Criar usuários de exemplo
        List<User> users = createSampleUsers();

        // Criar produtos de exemplo (mais variados)
        List<Product> products = createSampleProducts(users);

        // Criar mais produtos para leilões
        List<Product> auctionProducts = createAuctionProducts(users);
        products.addAll(auctionProducts);

        // Criar transações de exemplo (mais realistas)
        createSampleTransactions(users, products);

        // Criar histórico de transações (dados históricos)
        createHistoricalTransactions(users, products);

        // Criar transações de leilão ativas
        createActiveAuctions(users, auctionProducts);

        log.info("Dados de demonstração robustos criados com sucesso!");
        log.info("Total de usuários: {}", users.size());
        log.info("Total de produtos: {}", products.size());
        log.info("Total de transações: {}", transactionRepository.count());
    }

    private List<User> createSampleUsers() {
        List<User> users = new ArrayList<>();
        
        // Criar usuário mestre primeiro
        User mestre = new User();
        mestre.setUsername("mestre");
        mestre.setEmail("mestre@rpgmarket.com");
        mestre.setPassword(passwordEncoder.encode("123456"));
        mestre.setCharacterClass("Mestre do Reino");
        mestre.setRole(UserRole.ROLE_MESTRE);
        mestre.setLevel(50);
        mestre.setExperience(5000);
        mestre.setGoldCoins(new BigDecimal("10000"));
        
        mestre = userRepository.save(mestre);
        users.add(mestre);
        createDeliveryAddress(mestre);
        
        // Usuários com diferentes níveis e quantidades de ouro
        String[] usernames = {
            "ArthurCavaleiro", "MerlinMago", "LancelotGuerreiro", "GwenArqueira", 
            "TristanBardo", "GalahadPaladino", "GarethLadrao", "PercivalMonge",
            "KayEscudeiro", "BedivereCapitao", "GawainBarbaro", "LamorakRanger",
            "MorganaFeiticeira", "GarethCacador", "ElaineCurandeira", "LynetteBruxa",
            "DindraneProfetisa", "YsabeauAlquimista", "BlanchefleurMenestrel", "LaurielAssassina"
        };

        String[] classes = {
            "Cavaleiro", "Mago", "Guerreiro", "Arqueira", "Bardo", "Paladino",
            "Ladrão", "Monge", "Escudeiro", "Capitão", "Bárbaro", "Ranger",
            "Feiticeira", "Caçador", "Curandeira", "Bruxa", "Profetisa", "Alquimista", 
            "Menestrel", "Assassina"
        };

        for (int i = 0; i < usernames.length; i++) {
            User user = new User();
            user.setUsername(usernames[i]);
            user.setEmail(usernames[i].toLowerCase() + "@rpgmarket.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setCharacterClass(classes[i]);
            user.setRole(UserRole.ROLE_AVENTUREIRO);
            
            // Níveis variados (1-20)
            user.setLevel(random.nextInt(20) + 1);
            user.setExperience(user.getLevel() * 100);
            
            // Quantidades variadas de ouro (100-5000)
            BigDecimal goldAmount = new BigDecimal(random.nextInt(4900) + 100);
            user.setGoldCoins(goldAmount);
            
            user = userRepository.save(user);
            users.add(user);
            
            // Criar endereço para o usuário
            createDeliveryAddress(user);
        }

        return users;
    }

    private void createDeliveryAddress(User user) {
        DeliveryAddress address = new DeliveryAddress();
        address.setUserId(user.getId());
        address.setStreet("Rua dos Aventureiros, " + (random.nextInt(900) + 100));
        address.setNumber(String.valueOf(random.nextInt(999) + 1));
        address.setDistrict("Distrito Medieval");
        address.setCity("Reino de Camelot");
        address.setState("Reino Encantado");
        address.setPostalCode(String.format("%05d-%03d", random.nextInt(99999), random.nextInt(999)));
        address.setIsDefault(true);
        address.setDescription("Casa do " + user.getCharacterClass());
        
        deliveryAddressRepository.save(address);
    }

    private List<Product> createSampleProducts(List<User> users) {
        List<Product> products = new ArrayList<>();

        String[] itemNames = {
            "Excalibur", "Varinha de Merlin", "Machado de Guerra Élfico", "Arco Élfico Longo",
            "Alaúde Encantado", "Escudo Sagrado", "Adaga Sombria", "Luvas do Monge",
            "Armadura de Couro", "Capa do Capitão", "Martelo de Guerra", "Botas Élficas",
            "Poção de Cura Grande", "Pergaminho de Fogo", "Anel de Proteção", "Amuleto da Sorte",
            "Elmo de Dragão", "Espada Flamejante", "Cajado Arcano", "Besta Pesada",
            "Armadura de Placas Dourada", "Grimório das Sombras", "Colar da Sabedoria", 
            "Botas da Velocidade", "Gema do Poder", "Elixir da Imortalidade"
        };

        String[] descriptions = {
            "Um item lendário forjado pelos mestres artesãos do reino.",
            "Artefato mágico de poder incomensurável.",
            "Equipamento élfico de qualidade excepcional.",
            "Item encantado com propriedades únicas.",
            "Relíquia ancestral com poderes místicos.",
            "Equipamento de combate refinado e eficaz."
        };

        ProductCategory[] categories = ProductCategory.values();
        ItemRarity[] rarities = ItemRarity.values();

        for (int i = 0; i < itemNames.length; i++) {
            User seller = users.get(random.nextInt(users.size()));
            
            Product product = new Product();
            product.setName(itemNames[i]);
            product.setDescription(descriptions[random.nextInt(descriptions.length)] + 
                " Possui propriedades mágicas que ajudam aventureiros em suas jornadas. " +
                "Este item foi cuidadosamente selecionado e testado pelo vendedor.");
            
            // Preços mais variados baseados na raridade
            BigDecimal basePrice = new BigDecimal(random.nextInt(400) + 50);
            ItemRarity rarity = rarities[random.nextInt(rarities.length)];
            switch (rarity) {
                case LENDARIO:
                    basePrice = basePrice.multiply(new BigDecimal("5"));
                    break;
                case MUITO_RARO:
                    basePrice = basePrice.multiply(new BigDecimal("3"));
                    break;
                case RARO:
                    basePrice = basePrice.multiply(new BigDecimal("2"));
                    break;
                case INCOMUM:
                    basePrice = basePrice.multiply(new BigDecimal("1.5"));
                    break;
                default:
                    // COMUM mantém preço base
                    break;
            }
            
            product.setPrice(basePrice);
            product.setCategory(categories[random.nextInt(categories.length)]);
            product.setRarity(rarity);
            product.setType(ProductType.DIRECT_SALE);
            product.setStatus(i < 18 ? ProductStatus.SOLD : ProductStatus.AVAILABLE); // 18 vendidos, 8 disponíveis
            product.setSeller(seller);
            product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(60))); // até 60 dias atrás
            
            // Imagem padrão
            product.setImageUrl("/images/produto_teste.png");
            
            product = productRepository.save(product);
            products.add(product);
        }

        return products;
    }

    private List<Product> createAuctionProducts(List<User> users) {
        List<Product> auctionProducts = new ArrayList<>();

        String[] auctionItems = {
            "Coroa do Rei Dragão", "Cetro do Arcano Supremo", "Armadura do Guardião Eterno",
            "Lâmina da Tempestade", "Orbe da Verdade Eterna", "Anel do Senhor dos Anéis",
            "Cálice da Vida Infinita", "Manto da Invisibilidade", "Botas dos Sete Ventos",
            "Machado do Titã Ancestral", "Grimório Proibido", "Amuleto do Dragão Ancião"
        };

        for (int i = 0; i < auctionItems.length; i++) {
            User seller = users.get(random.nextInt(users.size()));
            
            Product product = new Product();
            product.setName(auctionItems[i]);
            product.setDescription("Item extremamente raro disponível apenas em leilão! " +
                "Este artefato possui história e poder únicos, sendo disputado pelos maiores aventureiros do reino. " +
                "Não perca esta oportunidade única de adquirir uma relíquia verdadeiramente especial.");
            
            // Preços iniciais de leilão
            product.setPrice(new BigDecimal(random.nextInt(200) + 100)); // Lance inicial
            product.setCategory(ProductCategory.values()[random.nextInt(ProductCategory.values().length)]);
            product.setRarity(i < 4 ? ItemRarity.LENDARIO : (i < 8 ? ItemRarity.MUITO_RARO : ItemRarity.RARO));
            product.setType(ProductType.AUCTION);
            product.setStatus(i < 6 ? ProductStatus.SOLD : ProductStatus.AVAILABLE); // 6 finalizados, 6 ativos
            product.setSeller(seller);
            product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            
            // Para leilões, usar campo de data de criação para simular fim
            // (removido auctionEndTime pois não existe no modelo atual)
            
            product.setImageUrl("/images/produto_teste.png");
            
            product = productRepository.save(product);
            auctionProducts.add(product);
        }

        return auctionProducts;
    }

    private void createSampleTransactions(List<User> users, List<Product> products) {
        log.info("Criando transações de exemplo...");
        
        List<Product> soldProducts = products.stream()
            .filter(p -> p.getStatus() == ProductStatus.SOLD)
            .toList();

        for (Product product : soldProducts) {
            try {
                // Escolher um comprador diferente do vendedor com limite de tentativas
                User buyer = null;
                int attempts = 0;
                do {
                    buyer = users.get(random.nextInt(users.size()));
                    attempts++;
                } while (buyer.getId().equals(product.getSeller().getId()) && attempts < 10);
                
                // Se não conseguiu encontrar comprador diferente, pular
                if (buyer.getId().equals(product.getSeller().getId())) {
                    log.warn("Pulando transação - não foi possível encontrar comprador diferente do vendedor");
                    continue;
                }

                // Buscar endereço do comprador
                DeliveryAddress address = deliveryAddressRepository.findByUserIdAndIsDefaultTrue(buyer.getId())
                    .orElse(null);

                Transaction transaction = new Transaction();
                transaction.setProduct(product);
                transaction.setBuyer(buyer);
                transaction.setSeller(product.getSeller());
                transaction.setAmount(product.getPrice());
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCreatedAt(product.getCreatedAt().plusHours(random.nextInt(24)));
                transaction.setCompletedAt(transaction.getCreatedAt().plusDays(random.nextInt(7) + 1));
                transaction.setDeliveryAddress(address);
                transaction.setNotes("Transação concluída com sucesso!");

                // Simular código de rastreio para algumas transações
                if (random.nextBoolean()) {
                    transaction.setTrackingCode("RPG" + String.format("%08d", random.nextInt(99999999)));
                }

                transactionRepository.save(transaction);
                
            } catch (Exception e) {
                log.warn("Erro ao criar transação para produto {}: {}", product.getName(), e.getMessage());
            }
        }

        // Criar algumas transações pendentes também
        List<Product> availableProducts = products.stream()
            .filter(p -> p.getStatus() == ProductStatus.AVAILABLE)
            .limit(3)
            .toList();

        for (Product product : availableProducts) {
            try {
                User buyer = null;
                int attempts = 0;
                do {
                    buyer = users.get(random.nextInt(users.size()));
                    attempts++;
                } while (buyer.getId().equals(product.getSeller().getId()) && attempts < 10);
                
                // Se não conseguiu encontrar comprador diferente, pular
                if (buyer.getId().equals(product.getSeller().getId())) {
                    continue;
                }

                DeliveryAddress address = deliveryAddressRepository.findByUserIdAndIsDefaultTrue(buyer.getId())
                    .orElse(null);

                Transaction transaction = new Transaction();
                transaction.setProduct(product);
                transaction.setBuyer(buyer);
                transaction.setSeller(product.getSeller());
                transaction.setAmount(product.getPrice());
                transaction.setStatus(random.nextBoolean() ? TransactionStatus.PENDING : TransactionStatus.SHIPPED);
                transaction.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(5)));
                transaction.setDeliveryAddress(address);
                transaction.setNotes("Aguardando processamento...");

                if (transaction.getStatus() == TransactionStatus.SHIPPED) {
                    transaction.setTrackingCode("RPG" + String.format("%08d", random.nextInt(99999999)));
                }

                transactionRepository.save(transaction);

                // Marcar produto como vendido se for uma transação real
                if (random.nextBoolean()) {
                    product.setStatus(ProductStatus.SOLD);
                    productRepository.save(product);
                }
                
            } catch (Exception e) {
                log.warn("Erro ao criar transação pendente para produto {}: {}", product.getName(), e.getMessage());
            }
        }
        
        log.info("Transações de exemplo criadas com sucesso!");
    }

    private void createHistoricalTransactions(List<User> users, List<Product> products) {
        log.info("Criando transações históricas...");
        
        // OTIMIZAÇÃO: Limitar número de transações para evitar sobrecarga
        int maxTransactionsBatch = 50; // Máximo de 50 transações por execução
        
        // Criar transações dos últimos 3 meses para analytics
        for (int month = 0; month < 3; month++) {
            int transactionsThisMonth = Math.min(random.nextInt(10) + 5, 15); // 5-15 transações por mês (reduzido)
            
            for (int i = 0; i < transactionsThisMonth && i < maxTransactionsBatch; i++) {
                try {
                    User buyer = users.get(random.nextInt(users.size()));
                    User seller = users.get(random.nextInt(users.size()));
                    
                    // Evitar auto-transação com limite de tentativas
                    int attempts = 0;
                    while (buyer.getId().equals(seller.getId()) && attempts < 5) {
                        seller = users.get(random.nextInt(users.size()));
                        attempts++;
                    }
                    
                    // Se ainda for auto-transação, pular
                    if (buyer.getId().equals(seller.getId())) {
                        continue;
                    }
                    
                    Product product = products.get(random.nextInt(products.size()));
                    
                    DeliveryAddress address = deliveryAddressRepository.findByUserIdAndIsDefaultTrue(buyer.getId())
                        .orElse(null);
                    
                    Transaction transaction = new Transaction();
                    transaction.setProduct(product);
                    transaction.setBuyer(buyer);
                    transaction.setSeller(seller);
                    transaction.setAmount(new BigDecimal(random.nextInt(800) + 50)); // Valores históricos variados
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    
                    // Data histórica - últimos 3 meses
                    LocalDateTime transactionDate = LocalDateTime.now()
                        .minusMonths(month)
                        .minusDays(random.nextInt(30))
                        .minusHours(random.nextInt(24));
                        
                    transaction.setCreatedAt(transactionDate);
                    transaction.setCompletedAt(transactionDate.plusDays(random.nextInt(7) + 1));
                    transaction.setDeliveryAddress(address);
                    transaction.setNotes("Transação histórica - " + (month == 0 ? "mês atual" : month + " meses atrás"));
                    transaction.setTrackingCode("HIST" + String.format("%08d", random.nextInt(99999999)));
                    
                    transactionRepository.save(transaction);
                    
                } catch (Exception e) {
                    log.warn("Erro ao criar transação histórica: {}", e.getMessage());
                    // Continua o loop mesmo com erro
                }
            }
        }
        log.info("Transações históricas criadas com sucesso!");
    }

    private void createActiveAuctions(List<User> users, List<Product> auctionProducts) {
        log.info("Criando leilões ativos com lances...");
        
        List<Product> activeAuctions = auctionProducts.stream()
            .filter(p -> p.getStatus() == ProductStatus.AVAILABLE)
            .toList();
            
        for (Product auction : activeAuctions) {
            // Criar alguns lances para cada leilão ativo
            int numBids = random.nextInt(8) + 2; // 2-10 lances por leilão
            BigDecimal currentPrice = auction.getPrice();
            
            for (int i = 0; i < numBids; i++) {
                User bidder = users.get(random.nextInt(users.size()));
                
                // Evitar lance do próprio vendedor
                while (bidder.getId().equals(auction.getSeller().getId())) {
                    bidder = users.get(random.nextInt(users.size()));
                }
                
                // Incrementar preço a cada lance
                currentPrice = currentPrice.add(new BigDecimal(random.nextInt(100) + 10));
                
                DeliveryAddress address = deliveryAddressRepository.findByUserIdAndIsDefaultTrue(bidder.getId())
                    .orElse(null);
                
                Transaction bid = new Transaction();
                bid.setProduct(auction);
                bid.setBuyer(bidder);
                bid.setSeller(auction.getSeller());
                bid.setAmount(currentPrice);
                bid.setStatus(i == numBids - 1 ? TransactionStatus.PENDING : TransactionStatus.CANCELED); // Último lance fica pendente
                bid.setCreatedAt(auction.getCreatedAt().plusHours(i * 6 + random.nextInt(6))); // Lances espalhados no tempo
                bid.setDeliveryAddress(address);
                bid.setNotes(i == numBids - 1 ? "Lance vencedor atual" : "Lance superado");
                
                transactionRepository.save(bid);
            }
            
            // Atualizar preço atual do leilão
            auction.setPrice(currentPrice);
            productRepository.save(auction);
        }
        
        // Criar alguns leilões finalizados com vencedores
        List<Product> finishedAuctions = auctionProducts.stream()
            .filter(p -> p.getStatus() == ProductStatus.SOLD)
            .toList();
            
        for (Product auction : finishedAuctions) {
            User winner = users.get(random.nextInt(users.size()));
            
            while (winner.getId().equals(auction.getSeller().getId())) {
                winner = users.get(random.nextInt(users.size()));
            }
            
            DeliveryAddress address = deliveryAddressRepository.findByUserIdAndIsDefaultTrue(winner.getId())
                .orElse(null);
            
            // Preço final do leilão (maior que o inicial)
            BigDecimal finalPrice = auction.getPrice().add(new BigDecimal(random.nextInt(500) + 100));
            
            // Simular fim do leilão baseado na data de criação
            LocalDateTime auctionEndTime = auction.getCreatedAt().plusDays(random.nextInt(7) + 1);
            
            Transaction winningBid = new Transaction();
            winningBid.setProduct(auction);
            winningBid.setBuyer(winner);
            winningBid.setSeller(auction.getSeller());
            winningBid.setAmount(finalPrice);
            winningBid.setStatus(TransactionStatus.COMPLETED);
            winningBid.setCreatedAt(auctionEndTime.minusMinutes(random.nextInt(60)));
            winningBid.setCompletedAt(auctionEndTime.plusHours(random.nextInt(48)));
            winningBid.setDeliveryAddress(address);
            winningBid.setNotes("Leilão vencido! Parabéns ao arrematante.");
            winningBid.setTrackingCode("AUCT" + String.format("%08d", random.nextInt(99999999)));
            
            transactionRepository.save(winningBid);
            
            // Atualizar preço final do produto
            auction.setPrice(finalPrice);
            productRepository.save(auction);
        }
    }
}
