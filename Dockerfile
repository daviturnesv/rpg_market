# Dockerfile para Render.com
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivo pom.xml primeiro para cache de dependências
COPY pom.xml .

# Baixar dependências (esta camada será cacheada se pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src src

# Compilar aplicação
RUN mvn clean package -DskipTests

# Estágio de runtime
FROM eclipse-temurin:17-jre-alpine

# Instalar dependências necessárias
RUN apk add --no-cache curl

# Definir diretório de trabalho
WORKDIR /app

# Criar diretório para uploads
RUN mkdir -p /tmp/uploads

# Copiar JAR da fase de build
COPY --from=build /app/target/rpg_market-0.0.1-SNAPSHOT.jar app.jar

# Expor porta
EXPOSE 8080

# Definir variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Comando para executar a aplicação
CMD ["java", "-jar", "app.jar"]
