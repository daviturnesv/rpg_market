#!/bin/bash

# Script de inicialização otimizado para Render.com
echo "🌟 Iniciando RPG Market em produção..."

# Configurações de JVM otimizadas para containers
export JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=200"

# Configurações Spring Boot
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=${PORT:-8080}

# Encontrar o JAR
JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ Arquivo JAR não encontrado!"
    exit 1
fi

echo "🚀 Iniciando aplicação: $JAR_FILE"
echo "🔧 Profile ativo: $SPRING_PROFILES_ACTIVE"
echo "🌐 Porta: $SERVER_PORT"

# Iniciar aplicação
exec java $JAVA_OPTS -Dspring.profiles.active=prod -Dserver.port=$SERVER_PORT -jar "$JAR_FILE"
