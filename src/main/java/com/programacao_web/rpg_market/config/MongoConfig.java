package com.programacao_web.rpg_market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MongoConfig {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
    
    /**
     * Componente para criar índices MongoDB para otimização de performance
     */
    @Component
    public static class MongoIndexesConfig implements CommandLineRunner {

        private static final Logger log = LoggerFactory.getLogger(MongoIndexesConfig.class);

        @Autowired
        private MongoTemplate mongoTemplate;

        @Override
        public void run(String... args) throws Exception {
            createIndexes();
        }

        private void createIndexes() {
            try {
                // Índices para a coleção de produtos (otimizar painel do mestre)
                mongoTemplate.indexOps("products").ensureIndex(
                    new Index()
                        .on("status", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("category", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("type", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("createdAt", org.springframework.data.domain.Sort.Direction.DESC)
                        .named("status_category_type_created_idx")
                        .background()
                );
                
                // Índice para busca por vendedor
                mongoTemplate.indexOps("products").ensureIndex(
                    new Index("seller.username", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("seller_username_idx")
                        .background()
                );
                
                // Índice para data de criação (usado para ordenação)
                mongoTemplate.indexOps("products").ensureIndex(
                    new Index("createdAt", org.springframework.data.domain.Sort.Direction.DESC)
                        .named("created_at_desc_idx")
                        .background()
                );

                // Índices para a coleção de transações
                mongoTemplate.indexOps("transactions").ensureIndex(
                    new Index("status", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("transaction_status_idx")
                        .background()
                );
                
                mongoTemplate.indexOps("transactions").ensureIndex(
                    new Index("created_at", org.springframework.data.domain.Sort.Direction.DESC)
                        .named("transaction_created_at_idx")
                        .background()
                );

                // Índices para a coleção de usuários
                mongoTemplate.indexOps("users").ensureIndex(
                    new Index("username", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("username_unique_idx")
                        .unique()
                        .background()
                );
                
                mongoTemplate.indexOps("users").ensureIndex(
                    new Index("email", org.springframework.data.domain.Sort.Direction.ASC)
                        .named("email_unique_idx")
                        .unique()
                        .background()
                );

                log.info("✅ Índices MongoDB criados com sucesso para otimização de performance!");

            } catch (Exception e) {
                log.warn("⚠️ Aviso ao criar índices MongoDB (pode ser normal se já existem): {}", e.getMessage());
            }
        }
    }
}