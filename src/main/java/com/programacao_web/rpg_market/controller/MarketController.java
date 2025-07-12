package com.programacao_web.rpg_market.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.programacao_web.rpg_market.model.ItemRarity;
import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.model.UserRole;
import com.programacao_web.rpg_market.service.ProductService;
import com.programacao_web.rpg_market.service.UserService;
import com.programacao_web.rpg_market.util.ClassCategoryPermission;

@Controller
@RequestMapping("/mercado")
public class MarketController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    // Página principal do mercado
    @GetMapping
    public String showMarketplace(
            Model model, 
            @Qualifier("products") @PageableDefault(size = 6, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable productPageable,
            
            @Qualifier("auctions") @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable auctionPageable) {
        // Obter usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        boolean isMaster = false;
        
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                characterClass = user.getCharacterClass();
                UserRole userRole = user.getRole();
                isMaster = userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN;
            }
        }        
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        
        // MESTREs podem ver todas as categorias
        Set<ProductCategory> allowedCategories;
        if (isMaster) {
            allowedCategories = EnumSet.allOf(ProductCategory.class);
        } else {
            allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        }
        
        // Produtos de venda direta disponíveis - filtrados por permissões do usuário
        List<Product> productsList = new ArrayList<>(productService.findAvailable(productPageable).getContent());
        if (!isMaster) {
            productsList.removeIf(p -> p.getCategory() == null || !allowedCategories.contains(p.getCategory()));
        }
        model.addAttribute("products", productsList);
        
        // Leilões ativos filtrados
        List<Product> auctionsList = new ArrayList<>(productService.findActiveAuctions(auctionPageable).getContent());
        if (!isMaster) {
            auctionsList.removeIf(a -> a.getCategory() == null || !allowedCategories.contains(a.getCategory()));
        }
        model.addAttribute("auctions", auctionsList);
        
        // Categorias permitidas para navegação
        model.addAttribute("categories", allowedCategories);
        model.addAttribute("isMaster", isMaster);
        return "market/index";
    }
    
    // Exibe produtos de uma categoria específica
    @GetMapping("/categoria/{category}")
    public String showCategoryProducts(
            @PathVariable ProductCategory category,
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        
        // Obter usuário logado e verificar se é MESTRE
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        boolean isMaster = false;
        
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                characterClass = user.getCharacterClass();
                UserRole userRole = user.getRole();
                isMaster = userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN;
            }
        }
        
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        
        // MESTREs podem ver todas as categorias
        Set<ProductCategory> allowedCategories;
        if (isMaster) {
            allowedCategories = EnumSet.allOf(ProductCategory.class);
        } else {
            allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        }
        
        model.addAttribute("products", productService.findByCategory(category, pageable));
        model.addAttribute("currentCategory", category);
        model.addAttribute("categories", allowedCategories); // Usar categorias permitidas, não todas
        model.addAttribute("isMaster", isMaster);
        return "market/category";
    }
    
    // Busca produtos por nome
    @GetMapping("/buscar")
    public String searchProducts(
            @RequestParam(required = false) String keyword,
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        
        // Obter usuário logado e verificar se é MESTRE
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        boolean isMaster = false;
        
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                characterClass = user.getCharacterClass();
                UserRole userRole = user.getRole();
                isMaster = userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN;
            }
        }
        
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        
        // MESTREs podem ver todas as categorias
        Set<ProductCategory> allowedCategories;
        if (isMaster) {
            allowedCategories = EnumSet.allOf(ProductCategory.class);
        } else {
            allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            Page<Product> searchResults = productService.search(keyword, ProductStatus.AVAILABLE, pageable);
            List<Product> filteredResults = new ArrayList<>(searchResults.getContent());
            
            // MESTREs podem ver todos os resultados, outros usuários têm filtros por categoria
            if (!isMaster) {
                filteredResults.removeIf(p -> p.getCategory() == null || !allowedCategories.contains(p.getCategory()));
            }
            
            model.addAttribute("products", new PageImpl<>(filteredResults, pageable, filteredResults.size()));
            model.addAttribute("keyword", keyword);
        } else {
            Page<Product> availableProducts = productService.findAvailable(pageable);
            List<Product> filteredProducts = new ArrayList<>(availableProducts.getContent());
            
            // MESTREs podem ver todos os produtos, outros usuários têm filtros por categoria
            if (!isMaster) {
                filteredProducts.removeIf(p -> p.getCategory() == null || !allowedCategories.contains(p.getCategory()));
            }
            
            model.addAttribute("products", new PageImpl<>(filteredProducts, pageable, filteredProducts.size()));
        }
        
        model.addAttribute("categories", allowedCategories); // Usar categorias permitidas, não todas
        model.addAttribute("isMaster", isMaster);
        return "market/search";
    }
    
    // Exibe todos os leilões ativos com opções de filtro e ordenação
    @GetMapping("/masmorra-dos-leiloes")
    public String showAuctions(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ItemRarity rarity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean endingSoon,
            @RequestParam(required = false, defaultValue = "auctionEndDate,asc") String sort,
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        // Obter usuário logado e verificar se é MESTRE
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        boolean isMaster = false;
        
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                characterClass = user.getCharacterClass();
                UserRole userRole = user.getRole();
                isMaster = userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN;
            }
        }
        
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        
        // MESTREs podem ver todas as categorias
        Set<ProductCategory> allowedCategories;
        if (isMaster) {
            allowedCategories = EnumSet.allOf(ProductCategory.class);
        } else {
            allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        }
        
        // Processar parâmetros de ordenação
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        // Criar pageable com ordenação específica
        PageRequest pageRequest = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            Sort.by(direction, sortField)
        );
        // Usar o novo método de filtro
        List<Product> auctionsList = new ArrayList<>(productService.findAuctionsWithFilters(
            category, rarity, minPrice, maxPrice, endingSoon, pageRequest).getContent());
        
        // MESTREs podem ver todos os leilões, outros usuários têm filtros por categoria
        if (!isMaster) {
            auctionsList.removeIf(a -> a.getCategory() == null || !allowedCategories.contains(a.getCategory()));
        }
        
        Page<Product> auctions = new PageImpl<>(auctionsList, pageRequest, auctionsList.size());
        model.addAttribute("auctions", auctions);
        model.addAttribute("categories", allowedCategories);
        model.addAttribute("rarities", com.programacao_web.rpg_market.model.ProductRarity.values());
        model.addAttribute("isMaster", isMaster);
        return "market/auctions";
    }
    
    // Exibe ranking de vendedores
    @GetMapping("/ranking-dos-nobres")
    public String showRanking(Model model) {
        model.addAttribute("topSellers", productService.getTopSellers());
        model.addAttribute("topBuyers", productService.getTopBuyers());
        return "market/ranking";
    }
      /**
     * Exibe todos os itens de venda direta com opções de filtro e ordenação
     */
    @GetMapping("/vendas-diretas")
    public String showDirectSales(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ItemRarity rarity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Obter usuário logado e suas permissões de classe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        boolean isMaster = false;
        
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                characterClass = user.getCharacterClass();
                UserRole userRole = user.getRole();
                isMaster = userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN;
            }
        }
        
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        
        // MESTREs podem ver todas as categorias
        Set<ProductCategory> allowedCategories;
        if (isMaster) {
            allowedCategories = EnumSet.allOf(ProductCategory.class);
        } else {
            allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        }

        // Filtrar produtos de venda direta (não leilões)
        Page<Product> productsPage = productService.findDirectSalesWithFilters(
            category, 
            rarity,
            minPrice, 
            maxPrice, 
            pageable
        );
        
        // MESTREs podem ver todos os produtos, outros usuários têm filtros por categoria
        List<Product> productsList = new ArrayList<>(productsPage.getContent());
        if (!isMaster) {
            productsList.removeIf(p -> p.getCategory() == null || !allowedCategories.contains(p.getCategory()));
        }
        
        Page<Product> filteredProductsPage = new PageImpl<>(productsList, pageable, productsList.size());
        
        model.addAttribute("products", filteredProductsPage);
        model.addAttribute("categories", allowedCategories); // Mostra apenas categorias permitidas
        model.addAttribute("rarities", ItemRarity.values());
        model.addAttribute("isMaster", isMaster);
        
        return "market/direct-sales";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
