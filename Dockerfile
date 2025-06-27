# Dockerfile otimizado para Render.com
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivo pom.xml primeiro para cache de dependências
COPY pom.xml .

# Baixar dependências (esta camada será cacheada se pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src src

# Compilar aplicação com otimizações
RUN mvn clean package -DskipTests -Dspring.profiles.active=prod

# Estágio de runtime otimizado
FROM eclipse-temurin:17-jre-alpine

# Instalar dependências necessárias e criar usuário não-root
RUN apk add --no-cache curl dumb-init && \
    addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# Definir diretório de trabalho
WORKDIR /app

# Criar diretório para uploads com permissões adequadas
RUN mkdir -p /tmp/uploads && chown -R spring:spring /tmp/uploads

# Copiar JAR da fase de build
COPY --from=build /app/target/rpg_market-0.0.1-SNAPSHOT.jar app.jar

# Definir propriedade do arquivo para usuário spring
RUN chown spring:spring app.jar

# Mudar para usuário não-root
USER spring:spring

# Expor porta
EXPOSE $PORT

# Definir variáveis de ambiente padrão otimizadas
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=200"

# Comando para executar a aplicação com dumb-init para melhor manejo de sinais
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -Dserver.port=${PORT:-8080} -jar app.jar"]
