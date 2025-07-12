package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.BidRepository;
import com.programacao_web.rpg_market.repository.UserRepository;
import com.programacao_web.rpg_market.repository.ProductRepository;
import com.programacao_web.rpg_market.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/debug")
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping requestMappingHandlerMapping;

    @GetMapping("/verificar-dados-demo")
    @ResponseBody
    public Map<String, Object> verificarDadosDemo() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            log.info("=== Verificando dados de demonstração ===");
            
            // Contar usuários e produtos
            List<User> usuarios = userRepository.findAll();
            List<Product> produtos = productRepository.findAll();
            
            resultado.put("total_usuarios", usuarios.size());
            resultado.put("total_produtos", produtos.size());
            
            // Verificar usuários padrão
            Map<String, Object> usuariosPadrao = new HashMap<>();
            boolean adminExiste = usuarios.stream().anyMatch(u -> "admin".equals(u.getUsername()));
            boolean testUserExiste = usuarios.stream().anyMatch(u -> "testuser".equals(u.getUsername()));
            
            usuariosPadrao.put("admin_existe", adminExiste);
            usuariosPadrao.put("testuser_existe", testUserExiste);
            resultado.put("usuarios_padrao", usuariosPadrao);
            
            // Verificar produtos específicos de demonstração
            String[] produtosEsperados = {
                "Espada Longa de Aço Valiriano",
                "Arco Élfico de Galhos de Lunária", 
                "Poção de Cura Maior",
                "Mochila de Couro de Ogro Encantada",
                "Amuleto da Proteção Menor"
            };
            
            Map<String, Boolean> produtosPadrao = new HashMap<>();
            for (String nome : produtosEsperados) {
                boolean existe = produtos.stream().anyMatch(p -> nome.equals(p.getName()));
                produtosPadrao.put(nome, existe);
            }
            resultado.put("produtos_padrao_esperados", produtosPadrao);
            
            // Listar todos os produtos atuais
            List<Map<String, Object>> produtosAtuais = produtos.stream()
                .map(p -> {
                    Map<String, Object> prod = new HashMap<>();
                    prod.put("id", p.getId());
                    prod.put("name", p.getName());
                    prod.put("price", p.getPrice());
                    prod.put("type", p.getType());
                    prod.put("category", p.getCategory());
                    prod.put("rarity", p.getRarity());
                    prod.put("status", p.getStatus());
                    prod.put("seller", p.getSeller().getUsername());
                    return prod;
                })
                .toList();
            resultado.put("produtos_atuais", produtosAtuais);
            
            // Verificar integridade dos dados
            int produtosPadraoEncontrados = (int) produtosPadrao.values().stream()
                .mapToInt(b -> b ? 1 : 0)
                .sum();
            
            resultado.put("produtos_padrao_encontrados", produtosPadraoEncontrados);
            resultado.put("produtos_padrao_completos", produtosPadraoEncontrados == 5);
            
            // Status geral
            boolean dadosCompletos = adminExiste && testUserExiste && produtosPadraoEncontrados == 5;
            resultado.put("dados_demonstracao_completos", dadosCompletos);
            
            if (!dadosCompletos) {
                StringBuilder recomendacao = new StringBuilder("RECOMENDAÇÕES: ");
                if (!adminExiste) recomendacao.append("Criar usuário admin. ");
                if (!testUserExiste) recomendacao.append("Criar usuário testuser. ");
                if (produtosPadraoEncontrados < 5) {
                    recomendacao.append("Criar ").append(5 - produtosPadraoEncontrados)
                        .append(" produtos de demonstração em falta. ");
                }
                resultado.put("recomendacao", recomendacao.toString());
            }
            
            log.info("Verificação concluída - Dados completos: {}", dadosCompletos);
            
        } catch (Exception e) {
            log.error("Erro ao verificar dados de demonstração", e);
            resultado.put("erro", "Erro interno: " + e.getMessage());
        }
        
        return resultado;
    }

    @GetMapping("/product/{productId}")
    @ResponseBody
    public Map<String, Object> debugProduct(@PathVariable String productId, 
                                           @AuthenticationPrincipal UserDetails currentUser) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                debug.put("error", "Produto não encontrado");
                debug.put("timestamp", java.time.LocalDateTime.now());
                return debug;
            }
            
            Product product = productOpt.get();
            debug.put("product", Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "type", product.getType(),
                "status", product.getStatus(),
                "price", product.getPrice(),
                "seller", product.getSeller().getUsername(),
                "minBidIncrement", product.getMinBidIncrement(),
                "buyNowPrice", product.getBuyNowPrice()
            ));
            
            if (currentUser != null) {
                Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    debug.put("currentUser", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "goldBalance", user.getGoldBalance(),
                        "isOwner", user.getId().equals(product.getSeller().getId())
                    ));
                }
            }
            
            List<Bid> bids = bidRepository.findByProductOrderByAmountDesc(product);
            debug.put("bidCount", bids.size());
            debug.put("bids", bids.stream().limit(5).map(bid -> Map.of(
                "id", bid.getId(),
                "amount", bid.getAmount(),
                "bidder", bid.getBidder().getUsername(),
                "bidTime", bid.getBidTime(),
                "winning", bid.isWinning()
            )).toList());
            
            Optional<Bid> highestBid = bidRepository.findHighestBidForProduct(product);
            if (highestBid.isPresent()) {
                debug.put("highestBid", Map.of(
                    "amount", highestBid.get().getAmount(),
                    "bidder", highestBid.get().getBidder().getUsername()
                ));
            }
            
            debug.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Erro no debug: ", e);
            debug.put("error", e.getMessage());
            debug.put("exception", e.getClass().getSimpleName());
            debug.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return debug;
    }

    @GetMapping("/mappings")
    @ResponseBody
    public Map<String, Object> debugMappings() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            List<String> mappings = requestMappingHandlerMapping.getHandlerMethods().entrySet().stream()
                .map(entry -> entry.getKey().toString() + " -> " + entry.getValue().getMethod().getName())
                .sorted()
                .toList();
                
            debug.put("total_mappings", mappings.size());
            debug.put("mappings", mappings);
            debug.put("lance_mappings", mappings.stream()
                .filter(mapping -> mapping.contains("lance"))
                .toList());
            debug.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return debug;
    }
}
