package com.programacao_web.rpg_market.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache para otimização de performance
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Configuração básica de cache
    // O Spring Boot configurará automaticamente um cache simples em memória
}
