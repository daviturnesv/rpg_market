#!/bin/bash
# Start script para Render.com

echo "🎮 Iniciando RPG Market..."

# Criar diretório de upload se não existir
mkdir -p /tmp/uploads

# Iniciar a aplicação Spring Boot
java -jar target/rpg_market-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
