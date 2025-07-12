package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable);
    
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}], 'status': ?1}")
    Page<Product> searchByNameAndStatus(String keyword, ProductStatus status, Pageable pageable);
    
    List<Product> findByStatusAndAuctionEndDateLessThanEqual(ProductStatus status, LocalDateTime dateTime);
    List<Product> findBySeller(User seller);
    List<Product> findBySellerAndStatusIn(User seller, List<ProductStatus> statuses);
    
    // Método para verificar se existe produto com o nome específico
    boolean existsByName(String name);
    
    // MÉTODOS OTIMIZADOS PARA PERFORMANCE
    
    // Contagem otimizada por status
    long countByStatusIn(List<ProductStatus> statuses);
    
    // Contagem otimizada por categoria
    long countByCategory(ProductCategory category);
    
    // Top produtos mais caros (OTIMIZAÇÃO)
    List<Product> findTop5ByOrderByPriceDesc();
    
    // Busca por categoria com paginação
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
    
    // Busca por tipo com paginação  
    Page<Product> findByType(com.programacao_web.rpg_market.model.ProductType type, Pageable pageable);
    
    // Busca por vendedor com paginação (por username)
    @Query("{'seller.username': {$regex: ?0, $options: 'i'}}")
    Page<Product> findBySellerUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    // Query complexa para filtros combinados
    @Query("{ $and: [ " +
           "{ $or: [ { 'category': { $exists: false } }, { 'category': ?0 } ] }, " +
           "{ $or: [ { 'status': { $exists: false } }, { 'status': ?1 } ] }, " +
           "{ $or: [ { 'type': { $exists: false } }, { 'type': ?2 } ] }, " +
           "{ $or: [ { 'seller.username': { $exists: false } }, { 'seller.username': { $regex: ?3, $options: 'i' } } ] } " +
           "] }")
    Page<Product> findWithFilters(ProductCategory category, ProductStatus status, 
                                 com.programacao_web.rpg_market.model.ProductType type, 
                                 String sellerUsername, Pageable pageable);
}
