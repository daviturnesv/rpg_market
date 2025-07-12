#!/bin/bash

# Build script otimizado para Render.com
echo "🚀 Iniciando build para produção..."

# Configurar JAVA_OPTS para produção
export JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Configurar profile ativo
export SPRING_PROFILES_ACTIVE=prod

# Build com Maven
echo "📦 Executando build Maven..."
./mvnw clean package -DskipTests -Dspring.profiles.active=prod

# Verificar se o build foi bem-sucedido
if [ $? -eq 0 ]; then
    echo "✅ Build concluído com sucesso!"
    echo "📁 JAR criado: $(ls -la target/*.jar)"
else
    echo "❌ Erro no build!"
    exit 1
fi
