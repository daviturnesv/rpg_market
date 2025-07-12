package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.service.FileStorageService;
import com.programacao_web.rpg_market.service.ProductService;
import com.programacao_web.rpg_market.service.UserService;
import com.programacao_web.rpg_market.util.ClassCategoryPermission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.EnumSet;

@Controller
@RequestMapping("/item")
public class ProductController {    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping("/novo")
    public String showCreateProductForm(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        try {
            model.addAttribute("product", new Product());
            // Exibir categorias baseadas nas permissões do usuário
            String characterClass = null;
            UserRole userRole = null;
            if (currentUser != null) {
                Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
                if (userOpt.isPresent()) {
                    characterClass = userOpt.get().getCharacterClass();
                    userRole = userOpt.get().getRole();
                }
            }
            if (characterClass != null) characterClass = capitalize(characterClass.trim());
            
            // MESTREs podem criar anúncios de qualquer categoria
            Set<ProductCategory> allowedCategories;
            if (userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN) {
                allowedCategories = EnumSet.allOf(ProductCategory.class);
            } else {
                allowedCategories = com.programacao_web.rpg_market.util.ClassCategoryPermission.getAllowedCategories(characterClass);
            }
            
            model.addAttribute("categories", allowedCategories);
            model.addAttribute("rarities", ItemRarity.values());
            model.addAttribute("types", ProductType.values());
            model.addAttribute("magicProperties", MagicProperty.values());
            model.addAttribute("isMaster", userRole == UserRole.ROLE_MESTRE || userRole == UserRole.ROLE_ADMIN);
            return "product/create";
        } catch (Exception e) {
            throw e;
        }
    }
      @PostMapping("/novo")
    public String createProduct(
            @ModelAttribute Product product,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String[] magicProperties,
            @RequestParam(value = "directSalePrice", required = false) BigDecimal directSalePrice,
            @RequestParam(value = "startingBid", required = false) BigDecimal startingBid,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuário não encontrado no reino.");
                return "redirect:/item/novo";
            }
            User user = userOpt.get();
            
            // Validação: MESTREs podem criar leilões de qualquer categoria, outros usuários têm restrições
            boolean isMaster = user.getRole() == UserRole.ROLE_MESTRE || user.getRole() == UserRole.ROLE_ADMIN;
            if (product.getType() == ProductType.AUCTION && !isMaster && 
                !ClassCategoryPermission.isCategoryAllowed(user.getCharacterClass(), product.getCategory())) {
                redirectAttributes.addFlashAttribute("error", "Sua classe não pode criar leilões dessa categoria.");
                return "redirect:/item/novo";
            }
            
            if (product.getType() == ProductType.DIRECT_SALE) {
                if (directSalePrice == null || directSalePrice.compareTo(BigDecimal.ZERO) <= 0) {
                    redirectAttributes.addFlashAttribute("error", "Por favor, informe um preço válido para venda direta.");
                    return "redirect:/item/novo";
                }
                product.setPrice(directSalePrice);
            } else if (product.getType() == ProductType.AUCTION) {
                if (startingBid == null || startingBid.compareTo(BigDecimal.ZERO) <= 0) {
                    redirectAttributes.addFlashAttribute("error", "Por favor, informe um lance inicial válido para leilão.");
                    return "redirect:/item/novo";
                }
                product.setPrice(startingBid);
            }
            
            if (image != null && !image.isEmpty()) {
                String imageFilename = fileStorageService.storeFile(image);
                product.setImageUrl(imageFilename);
                log.debug("Imagem salva: {} -> {}", image.getOriginalFilename(), imageFilename);
            }
            
            if (magicProperties != null && magicProperties.length > 0) {
                for (String property : magicProperties) {
                    product.addMagicProperty(MagicProperty.valueOf(property));
                }
            }
            
            if (product.getType() == ProductType.AUCTION) {
                product.setStatus(ProductStatus.AUCTION_ACTIVE);                
                if (product.getAuctionEndDate() == null) {
                    product.setAuctionEndDate(LocalDateTime.now().plusDays(7));
                }
            } else {
                product.setStatus(ProductStatus.AVAILABLE);
            }
            
            product.setCreatedAt(LocalDateTime.now());
            productService.create(product, userOpt.get());
            
            redirectAttributes.addFlashAttribute("success", 
                "Seu item foi colocado à venda no mercado! Os aventureiros já podem vê-lo.");
            return "redirect:/aventureiro/inventario";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao processar a imagem do item. Os orcs devem ter interferido na transmissão.");
            return "redirect:/item/novo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao criar item: " + e.getMessage());
            return "redirect:/item/novo";
        }
    }
    
    @GetMapping("/{id}")
    public String showProduct(@PathVariable String id, Model model, 
                            @AuthenticationPrincipal UserDetails currentUser) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            return "error/404";
        }
        
        Product product = productOpt.get();
        model.addAttribute("product", product);
        
        if (product.getStatus() == ProductStatus.SOLD || 
            product.getStatus() == ProductStatus.AUCTION_ENDED) {
            
            if (currentUser != null) {
                Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
                if (userOpt.isPresent() && 
                    product.getSeller().getId().equals(userOpt.get().getId())) {
                    model.addAttribute("isOwner", true);
                }
            }
            
            // Para produtos vendidos, ainda mostra a página mas com indicação clara
            model.addAttribute("isSold", true);
        }
        
        // Se for leilão, busca os lances
        if (product.getType() == ProductType.AUCTION) {
            model.addAttribute("bids", productService.getProductBids(product));
        }
        
        return "product/details";
    }
    
    /**
     * Inicia processo de compra - redireciona para checkout
     */
    @PostMapping("/{id}/comprar")
    public String startPurchase(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            Optional<Product> productOpt = productService.findById(id);
            
            if (userOpt.isEmpty() || productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Produto ou usuário não encontrado");
                return "redirect:/item/" + id;
            }
            
            Product product = productOpt.get();
            User buyer = userOpt.get();
            
            // Verificações básicas
            if (buyer.getId().equals(product.getSeller().getId())) {
                redirectAttributes.addFlashAttribute("error", "Você não pode comprar seu próprio item");
                return "redirect:/item/" + id;
            }
            
            if (product.getType() == ProductType.DIRECT_SALE) {
                return "redirect:/checkout/comprar/" + id;
            } else if (product.getType() == ProductType.AUCTION && product.getBuyNowPrice() != null) {
                return "redirect:/checkout/comprar-agora/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Este item não está disponível para compra direta");
                return "redirect:/item/" + id;
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao processar compra: " + e.getMessage());
            return "redirect:/item/" + id;
        }
    }
      /**
     * Processa lance em leilão diretamente
     */
    @PostMapping("/{id}/lance")
    public String placeBid(
            @PathVariable String id,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Busca o usuário atual
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuário não encontrado.");
                return "redirect:/item/" + id;
            }
            
            // Busca o produto
            Optional<Product> productOpt = productService.findById(id);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Produto não encontrado.");
                return "redirect:/marketplace";
            }
            
            User bidder = userOpt.get();
            Product product = productOpt.get();
            
            // Valida se o valor do lance é válido
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Por favor, informe um valor válido para o lance.");
                return "redirect:/item/" + id;
            }
            
            // Processa o lance usando o ProductService
            productService.makeBid(product, bidder, amount);
            
            redirectAttributes.addFlashAttribute("success", 
                "Lance de " + amount + " moedas realizado com sucesso! Que a sorte esteja com você, aventureiro!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao processar lance: " + e.getMessage());
        }
        
        return "redirect:/item/" + id;
    }
    
    // Exibe formulário de edição de produto
    @GetMapping("/{id}/editar")
    public String showEditForm(@PathVariable String id, Model model, @AuthenticationPrincipal UserDetails currentUser) {
        Optional<Product> productOpt = productService.findById(id);
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        
        if (productOpt.isEmpty() || userOpt.isEmpty()) {
            return "error/404";
        }
        
        User user = userOpt.get();
        Product product = productOpt.get();
        
        // MESTREs podem editar qualquer produto, outros usuários só os próprios
        boolean isMaster = user.getRole() == UserRole.ROLE_MESTRE || user.getRole() == UserRole.ROLE_ADMIN;
        boolean isOwner = product.getSeller().getId().equals(user.getId());
        
        if (!isMaster && !isOwner) {
            return "error/403"; // Acesso negado
        }
        
        model.addAttribute("product", product);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("isMaster", isMaster);
        model.addAttribute("isOwner", isOwner);
        
        return "product/edit";
    }
    
    // Processa a edição de um produto
    @PostMapping("/{id}/editar")
    public String editProduct(
            @PathVariable String id,
            @ModelAttribute Product product,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            return "error/403";
        }
        
        User user = userOpt.get();
        Optional<Product> originalProductOpt = productService.findById(id);
        
        if (originalProductOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Produto não encontrado");
            return "redirect:/aventureiro/inventario";
        }
        
        Product originalProduct = originalProductOpt.get();
        
        // MESTREs podem editar qualquer produto, outros usuários só os próprios
        boolean isMaster = user.getRole() == UserRole.ROLE_MESTRE || user.getRole() == UserRole.ROLE_ADMIN;
        boolean isOwner = originalProduct.getSeller().getId().equals(user.getId());
        
        if (!isMaster && !isOwner) {
            return "error/403";
        }
        
        try {
            // Se uma nova imagem foi enviada, processa e atualiza o campo imageUrl
            if (image != null && !image.isEmpty()) {
                String imageFilename = fileStorageService.storeFile(image);
                product.setImageUrl(imageFilename);
            }
            
            productService.update(id, product, user);
            
            String successMessage = isMaster && !isOwner ? 
                "Item editado pelo MESTRE com sucesso!" : 
                "Item atualizado com sucesso!";
            redirectAttributes.addFlashAttribute("success", successMessage);
            
            if (isMaster && !isOwner) {
                log.info("MESTRE {} editou anúncio {} (proprietário: {})", 
                        user.getUsername(), product.getId(), originalProduct.getSeller().getUsername());
            }
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao fazer upload da imagem: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/item/" + id;
    }
    
    /**
     * Handles request to delete a product
     * MESTREs podem excluir qualquer anúncio, outros usuários só podem excluir seus próprios
     */
    @PostMapping("/{id}/excluir")
    public String deleteProduct(@PathVariable String id, 
                              @AuthenticationPrincipal UserDetails currentUser,
                              RedirectAttributes redirectAttributes) {
        try {
            // Primeiro, obtenha o usuário atual do UserService usando o nome de usuário do UserDetails
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuário não encontrado");
                return "redirect:/aventureiro/inventario";
            }
              User user = userOpt.get();
            
            Optional<Product> productOpt = productService.findById(id);
            
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Item não encontrado");
                return "redirect:/aventureiro/inventario";
            }
            
            Product product = productOpt.get();
              
            // Verifica permissões: MESTREs podem excluir qualquer item, outros usuários só os próprios
            boolean canDelete = false;
            boolean isMaster = user.getRole() == UserRole.ROLE_MESTRE || user.getRole() == UserRole.ROLE_ADMIN;
            boolean isOwner = product.getSeller().getId().equals(user.getId());
            
            if (isMaster) {
                canDelete = true;
                log.info("MESTRE {} excluindo anúncio {} (proprietário: {})", 
                        user.getUsername(), product.getId(), product.getSeller().getUsername());
            } else if (isOwner) {
                canDelete = true;
            }
            
            if (!canDelete) {
                redirectAttributes.addFlashAttribute("errorMessage", "Você não tem permissão para excluir este item");
                return "redirect:/item/" + id;
            }
            
            boolean result = productService.deleteProduct(product, user);  // Passa o user do userService
            
            if (result) {
                String messageType;
                if (isMaster && !isOwner) {
                    messageType = "Anúncio removido pelo MESTRE com sucesso";
                } else if (product.getType() == ProductType.AUCTION && product.getStatus() == ProductStatus.AUCTION_ACTIVE) {
                    messageType = "Leilão encerrado e removido com sucesso";
                } else {
                    messageType = "Anúncio removido com sucesso";
                }
                redirectAttributes.addFlashAttribute("successMessage", messageType);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Não foi possível remover o item");
            }
            
            // MESTREs são redirecionados para o painel de gestão de anúncios
            if (isMaster && !isOwner) {
                return "redirect:/mestre/gestao-anuncios";
            }
            
            return "redirect:/aventureiro/inventario";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao processar a exclusão: " + e.getMessage());
            return "redirect:/aventureiro/inventario";
        }
    }
    
    /**
     * Endpoint específico para MESTREs excluírem produtos do mercado
     */
    @PostMapping("/{id}/excluir-mestre")
    public String deleteProductFromMarket(@PathVariable String id, 
                                        @AuthenticationPrincipal UserDetails currentUser,
                                        RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuário não encontrado");
                return "redirect:/mercado";
            }
            
            User user = userOpt.get();
            
            // Verifica se é MESTRE
            if (user.getRole() != UserRole.ROLE_MESTRE && user.getRole() != UserRole.ROLE_ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "Acesso negado. Apenas MESTREs podem usar esta função.");
                return "redirect:/mercado";
            }
            
            Optional<Product> productOpt = productService.findById(id);
            
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Item não encontrado");
                return "redirect:/mercado";
            }
            
            Product product = productOpt.get();
            
            log.info("MESTRE {} excluindo anúncio {} do mercado (proprietário: {})", 
                    user.getUsername(), product.getId(), product.getSeller().getUsername());
            
            boolean result = productService.deleteProduct(product, user);
            
            if (result) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Anúncio removido do mercado pelo MESTRE com sucesso");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Não foi possível remover o item");
            }
            
            return "redirect:/mestre/gestao-anuncios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao processar a exclusão: " + e.getMessage());
            return "redirect:/mestre/gestao-anuncios";
        }
    }
    
    private String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
}
}
