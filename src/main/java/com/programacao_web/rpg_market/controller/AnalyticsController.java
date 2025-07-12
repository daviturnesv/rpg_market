package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.TransactionStatus;
import com.programacao_web.rpg_market.model.Bid;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.ProductType;
import com.programacao_web.rpg_market.service.UserService;
import com.programacao_web.rpg_market.service.ProductService;
import com.programacao_web.rpg_market.repository.UserRepository;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import com.programacao_web.rpg_market.repository.ProductRepository;
import com.programacao_web.rpg_market.repository.BidRepository;
import com.programacao_web.rpg_market.dto.AnalyticsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Controlador para análises e ranking - Acesso restrito para ROLE_MESTRE
 */
@Controller
@RequestMapping("/mestre")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private BidRepository bidRepository;

    /**
     * Página principal de análises (dashboard simplificado)
     * Restrito para usuários com ROLE_MESTRE
     */
    @GetMapping("/dashboard")
    public String showAnalyticsDashboard(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "7") int periodo,
            Model model) {
        
        try {
            log.info("Carregando dashboard de analytics para período: {} dias", periodo);
            
            // Verificação de autorização
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }

            User user = userOpt.get();
            if (!user.getRole().toString().equals("ROLE_MESTRE")) {
                return "error/403";
            }

            // OTIMIZAÇÃO: Dados básicos usando apenas COUNT queries
            AnalyticsData analytics = new AnalyticsData();
            analytics.setTotalUsuarios(userRepository.count());
            analytics.setTotalProdutos(productRepository.count());
            analytics.setTotalTransacoes(transactionRepository.count());
            analytics.setLeiloesAtivos(bidRepository.count());
            analytics.setLeiloesFinalizados(0L);
            
            // OTIMIZAÇÃO: Transações do período usando query específica
            LocalDateTime dataInicio = LocalDateTime.now().minusDays(periodo);
            long transacoesPeriodoCount = transactionRepository.countByCreatedAtAfter(dataInicio);
            
            // OTIMIZAÇÃO: Valor médio simplificado
            BigDecimal valorMedio = BigDecimal.ZERO;
            if (transacoesPeriodoCount > 0) {
                // Buscar apenas soma dos valores do período
                List<Transaction> transacoesPeriodo = transactionRepository.findByCreatedAtAfterOrderByCreatedAtDesc(dataInicio);
                if (!transacoesPeriodo.isEmpty()) {
                    BigDecimal soma = transacoesPeriodo.stream()
                        .map(Transaction::getAmount)
                        .filter(amount -> amount != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (soma.compareTo(BigDecimal.ZERO) > 0) {
                        valorMedio = soma.divide(BigDecimal.valueOf(transacoesPeriodo.size()), 2, RoundingMode.HALF_UP);
                    }
                }
            }
            analytics.setValorMedioTransacao(valorMedio);
            
            // Taxa de atividade
            BigDecimal taxaAtividade = BigDecimal.ZERO;
            if (periodo > 0) {
                taxaAtividade = BigDecimal.valueOf(transacoesPeriodoCount)
                    .divide(BigDecimal.valueOf(periodo), 2, RoundingMode.HALF_UP);
            }
            analytics.setTaxaAtividade(taxaAtividade);
            
            // OTIMIZAÇÃO: Top vendedores usando query limitada
            List<User> topVendedores = transactionRepository.findTopSellers();
            if (topVendedores.isEmpty()) {
                topVendedores = userRepository.findAll().stream()
                    .limit(5)
                    .collect(Collectors.toList());
            }
            
            // OTIMIZAÇÃO: Produtos mais caros usando query limitada
            List<Product> produtosMaisCaros = productRepository.findTop5ByOrderByPriceDesc();
            
            // OTIMIZAÇÃO: Volume total simplificado
            BigDecimal volumeTotal = BigDecimal.valueOf(analytics.getTotalProdutos() * 100); // Estimativa rápida
            analytics.setVolumeTotalVendas(volumeTotal);
            
            // OTIMIZAÇÃO: Produtos por categoria usando aggregation
            Map<String, Long> produtosPorCategoria = Map.of(
                "ARMAS", productRepository.countByCategory(ProductCategory.ARMAS),
                "ARMADURA_VESTIMENTA", productRepository.countByCategory(ProductCategory.ARMADURA_VESTIMENTA),
                "POCOES_ELIXIRES", productRepository.countByCategory(ProductCategory.POCOES_ELIXIRES),
                "JOIAS_ARTEFATOS", productRepository.countByCategory(ProductCategory.JOIAS_ARTEFATOS),
                "DIVERSOS", productRepository.countByCategory(ProductCategory.DIVERSOS)
            );
            
            // Adicionar dados ao modelo
            model.addAttribute("analytics", analytics);
            model.addAttribute("currentUser", user);
            model.addAttribute("topVendedores", topVendedores);
            model.addAttribute("produtosMaisCaros", produtosMaisCaros);
            model.addAttribute("periodoSelecionado", periodo);
            model.addAttribute("transacoesPeriodo", transacoesPeriodoCount);
            model.addAttribute("produtosPorCategoria", produtosPorCategoria);
            
            log.info("Dashboard carregado com sucesso");
            return "analytics/dashboard";
            
        } catch (Exception e) {
            log.error("Erro no dashboard de analytics", e);
            model.addAttribute("error", "Erro interno: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Página de ranking detalhado dos nobres
     * Restrito para usuários com ROLE_MESTRE
     */
    @GetMapping("/ranking-nobres")
    public String showRankingDetalhado(
            @AuthenticationPrincipal UserDetails currentUser,
            Model model) {
        
        try {
            // Verificação de autorização
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }

            User user = userOpt.get();
            if (!user.getRole().toString().equals("ROLE_MESTRE")) {
                return "error/403";
            }

            // Diferentes rankings
            // Top vendedores baseado na quantidade de transações como vendedor (número de vendas realizadas)
            Map<User, Long> vendasPorUsuario = transactionRepository.findAll().stream()
                    .filter(t -> t.getSeller() != null && t.getStatus() == TransactionStatus.COMPLETED)
                    .collect(Collectors.groupingBy(
                        Transaction::getSeller,
                        Collectors.counting()
                    ));
                    
            List<User> topVendedores = vendasPorUsuario.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .map(Map.Entry::getKey)
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Se não houver vendedores baseado em transações, usar os usuários com mais produtos vendidos
            if (topVendedores.isEmpty()) {
                Map<User, Long> produtosPorUsuario = productRepository.findAll().stream()
                        .filter(p -> p.getSeller() != null && p.getStatus() == ProductStatus.SOLD)
                        .collect(Collectors.groupingBy(
                            Product::getSeller,
                            Collectors.counting()
                        ));
                        
                topVendedores = produtosPorUsuario.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .map(Map.Entry::getKey)
                        .limit(10)
                        .collect(Collectors.toList());
                        
                // Usar produtosPorUsuario como vendasPorUsuario se não há transações
                if (!produtosPorUsuario.isEmpty()) {
                    vendasPorUsuario = produtosPorUsuario;
                }
            }
            
            // Top compradores baseado em transações realizadas (como comprador)
            Map<User, Long> comprasPorUsuario = transactionRepository.findAll().stream()
                    .filter(t -> t.getBuyer() != null && t.getStatus() == TransactionStatus.COMPLETED)
                    .collect(Collectors.groupingBy(
                        Transaction::getBuyer,
                        Collectors.counting()
                    ));
                    
            List<User> topCompradores = comprasPorUsuario.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .map(Map.Entry::getKey)
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Se não houver compradores baseado em transações, usar nível como critério alternativo
            if (topCompradores.isEmpty()) {
                topCompradores = userRepository.findAll().stream()
                        .filter(u -> u.getLevel() > 0)
                        .sorted((u1, u2) -> Integer.compare(u2.getLevel(), u1.getLevel()))
                        .limit(10)
                        .collect(Collectors.toList());
            }
            
            // Usuários mais ricos baseado em quantidade de moedas de ouro
            List<User> usuariosMaisRicos = userRepository.findAll().stream()
                    .filter(u -> u.getGoldCoins() != null && u.getGoldCoins().compareTo(BigDecimal.ZERO) > 0)
                    .sorted((u1, u2) -> u2.getGoldCoins().compareTo(u1.getGoldCoins()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Estatísticas gerais
            long totalAventureiros = userRepository.count();
            long totalMestres = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().toString().equals("ROLE_MESTRE"))
                    .count();
            
            // Adicionar dados ao modelo
            model.addAttribute("currentUser", user);
            model.addAttribute("topVendedores", topVendedores);
            model.addAttribute("topCompradores", topCompradores);
            model.addAttribute("usuariosMaisRicos", usuariosMaisRicos);
            model.addAttribute("totalAventureiros", totalAventureiros);
            model.addAttribute("totalMestres", totalMestres);
            model.addAttribute("totalTransacoes", transactionRepository.count());
            model.addAttribute("totalLances", bidRepository.count());
            
            // Adicionar dados detalhados dos rankings para exibição
            model.addAttribute("vendasPorUsuario", vendasPorUsuario);
            model.addAttribute("comprasPorUsuario", comprasPorUsuario);
            
            // Volume total de vendas
            BigDecimal volumeTotalVendas = transactionRepository.findAll()
                    .stream()
                    .filter(t -> t.getAmount() != null && t.getStatus() == TransactionStatus.COMPLETED)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("volumeTotalVendas", volumeTotalVendas);
            
            return "analytics/ranking-nobres";
            
        } catch (Exception e) {
            // Log do erro para debug
            System.err.println("Erro no ranking-nobres: " + e.getMessage());
            // Log do erro sem stack trace
            model.addAttribute("error", "Erro interno: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Página de relatório de atividades
     * Restrito para usuários com ROLE_MESTRE
     */
    @GetMapping("/relatorio-atividades")
    public String showRelatorioAtividades(
            @AuthenticationPrincipal UserDetails currentUser,
            Model model) {
        
        try {
            // Verificação de autorização
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }

            User user = userOpt.get();
            if (!user.getRole().toString().equals("ROLE_MESTRE")) {
                return "error/403";
            }

            // Atividades recentes (últimos 7 dias)
            LocalDateTime seteDiasAtras = LocalDateTime.now().minusDays(7);
            
            List<Transaction> transacoesRecentes = transactionRepository.findAll()
                    .stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(seteDiasAtras))
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            List<Bid> lancesRecentes = bidRepository.findAll()
                    .stream()
                    .filter(b -> b.getBidTime() != null && b.getBidTime().isAfter(seteDiasAtras))
                    .sorted((b1, b2) -> b2.getBidTime().compareTo(b1.getBidTime()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            List<Product> produtosRecentes = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(seteDiasAtras))
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Adicionar dados ao modelo
            model.addAttribute("currentUser", user);
            model.addAttribute("totalUsuarios", userRepository.count());
            model.addAttribute("vendasHoje", transacoesRecentes.size());
            model.addAttribute("totalLances", lancesRecentes.size());
            model.addAttribute("novosUsuariosHoje", 0L);
            model.addAttribute("novosProdutosHoje", produtosRecentes.size());
            
            // Listas de atividades
            model.addAttribute("transacoesRecentes", transacoesRecentes);
            model.addAttribute("lancesRecentes", lancesRecentes);
            model.addAttribute("produtosRecentes", produtosRecentes);
            
            return "analytics/relatorio-atividades";
            
        } catch (Exception e) {
            System.err.println("Erro no relatório de atividades: " + e.getMessage());
            // Log do erro sem stack trace
            model.addAttribute("error", "Erro interno: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Página de gestão de anúncios - Para MESTREs administrarem todos os produtos
     * Restrito para usuários com ROLE_MESTRE
     * OTIMIZADO para melhor performance
     */
    @GetMapping("/gestao-anuncios")
    public String showGestaoAnuncios(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) String seller,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        
        try {
            // Verificação de autorização
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }

            User user = userOpt.get();
            if (!user.getRole().toString().equals("ROLE_MESTRE")) {
                return "error/403";
            }

            // OTIMIZAÇÃO: Usar consultas diretas ao banco em vez de carregar tudo em memória
            Page<Product> produtos;
            
            // Aplicar filtros diretamente na query
            if (category != null || status != null || type != null || (seller != null && !seller.trim().isEmpty())) {
                // Usar métodos customizados do repository para filtros complexos
                produtos = productService.findProductsWithFilters(category, status, type, seller, pageable);
            } else {
                // Busca simples paginada
                produtos = productRepository.findAll(pageable);
            }

            // Adicionar dados ao modelo
            model.addAttribute("currentUser", user);
            model.addAttribute("produtos", produtos);
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("statuses", ProductStatus.values());
            model.addAttribute("types", ProductType.values());
            
            // Filtros atuais
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedType", type);
            model.addAttribute("selectedSeller", seller);
            
            // OTIMIZAÇÃO: Estatísticas rápidas usando queries diretas
            long totalAnuncios = productRepository.count();
            long anunciosAtivos = productRepository.countByStatusIn(
                List.of(ProductStatus.AVAILABLE, ProductStatus.AUCTION_ACTIVE)
            );
            long anunciosVendidos = productRepository.countByStatusIn(
                List.of(ProductStatus.SOLD, ProductStatus.AUCTION_ENDED)
            );
            
            model.addAttribute("totalAnuncios", totalAnuncios);
            model.addAttribute("anunciosAtivos", anunciosAtivos);
            model.addAttribute("anunciosVendidos", anunciosVendidos);
            
            return "analytics/gestao-anuncios";
            
        } catch (Exception e) {
            log.error("Erro na gestão de anúncios", e);
            model.addAttribute("error", "Erro interno do servidor. Tente novamente.");
            return "error/500";
        }
    }
}