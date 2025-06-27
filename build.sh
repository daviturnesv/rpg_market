#!/bin/bash
# Build script para Render.com

echo "🚀 Iniciando build do RPG Market..."

# Instalar dependências Maven e compilar
echo "📦 Instalando dependências e compilando..."
./mvnw clean package -DskipTests

echo "✅ Build concluído com sucesso!"
