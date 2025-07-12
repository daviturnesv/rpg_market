package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.TransactionStatus;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    // Busca transações de um comprador
    List<Transaction> findByBuyer(User buyer);
    
    // Busca transações de um vendedor
    List<Transaction> findBySeller(User seller);
    
    // Busca transações por status
    List<Transaction> findByStatus(TransactionStatus status);
    
    // OTIMIZAÇÃO: Busca transações por período
    List<Transaction> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime startDate);
    
    // OTIMIZAÇÃO: Contagem de transações por período
    long countByCreatedAtAfter(LocalDateTime startDate);
    
    // Busca os top vendedores por número de vendas completadas
    @Aggregation(pipeline = {
        "{ $match: { 'status': 'COMPLETED' } }",
        "{ $group: { _id: '$seller', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }",
        "{ $limit: ?0 }"
    })
    List<User> findTopSellers(int limit);
    
    // Busca os top compradores por número de compras completadas
    @Aggregation(pipeline = {
        "{ $match: { 'status': 'COMPLETED' } }",
        "{ $group: { _id: '$buyer', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }",
        "{ $limit: ?0 }"
    })
    List<User> findTopBuyers(int limit);
    
    // Métodos de conveniência para ranking
    default List<User> findTopSellers() {
        return findTopSellers(10);
    }
    
    default List<User> findTopBuyers() {
        return findTopBuyers(10);
    }
}
