# 🗡️ RPG Market - Marketplace de Itens RPG

Um marketplace medieval fantástico para compra e venda de itens RPG, desenvolvido com Spring Boot e MongoDB.

## 🎯 **Funcionalidades Principais**

### 🧙‍♂️ **Painel do Mestre (Administração)**
- 📊 **Dashboard Completo** - Métricas em tempo real com filtros de período (7, 14, 30 dias)
- 🏆 **Ranking dos Nobres** - Rankings interativos de top vendedores, compradores e usuários mais ricos
- 📈 **Relatório de Atividades** - Monitoramento de transações, lances e produtos recentes
- 📋 **Análises Financeiras** - Volume de vendas, valor médio de transações e taxa de atividade
- 🎯 **Interface unificada** - Layout consistente e responsivo em todas as páginas

### ⚔️ **Sistema de Mercado**
- �️ **Mercado de Itens** - Navegação por categorias (armas, armaduras, poções, etc.)
- 💰 **Sistema de Compra Direta** - Transações instantâneas
- 🔨 **Sistema de Leilões** - Lances em tempo real com auto-atualização
- 📦 **Gestão de Inventário** - Controle completo de produtos pessoais
- 🏷️ **Upload de Imagens** - Suporte para imagens de produtos

### 👤 **Sistema de Usuários**
- 🔐 **Autenticação Segura** - Login/registro com Spring Security
- 🎭 **Perfis RPG** - Classes de personagem, níveis e moedas de ouro
- 📊 **Estatísticas Pessoais** - Histórico de compras, vendas e ranking
- 🏠 **Sistema de Endereços** - Gerenciamento de endereços de entrega

## 🚀 **Instalação Rápida**

### **Pré-requisitos**
- ☕ **Java JDK 17+**
- 🍃 **MongoDB 7.0+**
- 🔧 **Git** (opcional)

### **1. Clone e Configure**
```bash
# Clone o repositório
git clone <url-do-repositorio>
cd rpg_market

# Inicie o MongoDB (se local)
# Windows
net start MongoDB
# Linux/macOS
sudo systemctl start mongod
```

### **2. Configure o Banco de Dados**

#### **Desenvolvimento Local**
Edite `src/main/resources/application.properties`:
```properties
# MongoDB Local
spring.data.mongodb.uri=mongodb://localhost:27017/rpgmarket

# MongoDB Atlas (nuvem)
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/rpgmarket
```

### **3. Execute a Aplicação**
```bash
# Usando Maven Wrapper (recomendado)
./mvnw spring-boot:run

# Ou compile e execute
./mvnw clean package
java -jar target/rpg_market-0.0.1-SNAPSHOT.jar
```

### **4. Acesse o Sistema**
- 🌐 **URL**: http://localhost:8080
- 👑 **Admin**: `admin` / `admin`
- 🧙‍♂️ **Usuário Teste**: `testuser` / `password`
## 📁 **Estrutura do Projeto**

```
rpg_market/
├── src/main/
│   ├── java/com/programacao_web/rpg_market/
│   │   ├── config/          # Configurações (Security, Web, etc.)
│   │   ├── controller/      # Controllers REST e MVC
│   │   │   ├── AnalyticsController.java  # Painel do Mestre
│   │   │   ├── MarketController.java     # Mercado
│   │   │   └── UserController.java       # Usuários
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── model/          # Entidades (User, Product, Transaction, etc.)
│   │   ├── repository/     # Repositórios MongoDB
│   │   ├── service/        # Lógica de negócio
│   │   │   ├── AnalyticsService.java     # Métricas e relatórios
│   │   │   ├── ProductService.java       # Gestão de produtos
│   │   │   └── TransactionService.java   # Transações
│   │   └── RpgMarketApplication.java
│   └── resources/
│       ├── static/
│       │   ├── css/
│       │   │   ├── master-panel.css      # Estilos do Painel do Mestre
│       │   │   ├── market.css            # Estilos do Mercado
│       │   │   └── common.css            # Estilos gerais
│       │   ├── js/             # Scripts JavaScript
│       │   └── images/         # Imagens estáticas
│       ├── templates/
│       │   ├── analytics/      # Templates do Painel do Mestre
│       │   │   ├── dashboard.html
│       │   │   ├── ranking-nobres.html
│       │   │   └── relatorio-atividades.html
│       │   ├── market/         # Templates do Mercado
│       │   ├── user/           # Templates de usuário
│       │   └── layout/         # Layouts base
│       └── application.properties
├── uploads/images/             # Upload de imagens (criado automaticamente)
└── pom.xml
```

## 🎮 **Guia de Uso**

### **👑 Painel do Mestre (Administradores)**

1. **Dashboard Principal**
   - Acesse `/mestre/dashboard`
   - Visualize métricas gerais do sistema
   - Use filtros de período (7, 14, 30 dias)
   - Monitore volume de vendas e transações

2. **Rankings dos Nobres**
   - Acesse `/mestre/ranking-nobres`
   - Alterne entre rankings: vendedores, compradores, mais ricos
   - Visualize estatísticas detalhadas dos usuários

3. **Relatório de Atividades**
   - Acesse `/mestre/relatorio-atividades`
   - Monitore transações, lances e produtos recentes
   - Acompanhe atividade em tempo real

### **⚔️ Sistema de Mercado (Usuários)**

1. **Navegação**
   - Explore categorias de itens
   - Use filtros de busca
   - Visualize detalhes de produtos

2. **Compras**
   - Compra direta ou leilão
   - Sistema de lances automático
   - Confirmação de pagamento

3. **Vendas**
   - Cadastre novos produtos
   - Upload de imagens
   - Configure preços e leilões

## 🛠️ **Tecnologias Utilizadas**

### **Backend**
- ⚡ **Spring Boot 3.2.4** - Framework principal
- 🔐 **Spring Security** - Autenticação e autorização  
- 🍃 **Spring Data MongoDB** - Persistência NoSQL
- 📊 **MongoDB** - Banco de dados principal

### **Frontend**
- 🎨 **Thymeleaf** - Template engine
- 🎯 **Bootstrap 5.3** - Framework CSS responsivo
- ✨ **Font Awesome** - Biblioteca de ícones
- 🎪 **JavaScript** - Interatividade e AJAX

### **Recursos Especiais**
- 📸 **Upload de Imagens** - Gestão de arquivos
- 🔄 **Auto-refresh** - Atualizações em tempo real
- 📱 **Design Responsivo** - Mobile-first
- 🎭 **Tema Medieval** - Interface temática RPG

## 🔧 **Configurações Avançadas**

### **MongoDB Atlas (Nuvem)**
```properties
# Substitua no application.properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/rpgmarket
```

### **Configuração de Upload**
```properties
# Diretório personalizado para uploads
rpg.market.file.upload-dir=C:/uploads/rpg-images
```

### **Configuração de Porta**
```properties
# Alterar porta padrão
server.port=8081
```

## 🐛 **Solução de Problemas**

### **MongoDB não conecta**
```bash
# Verifique se está rodando
# Windows
net start MongoDB
# Linux/macOS  
sudo systemctl start mongod
```

### **Porta 8080 ocupada**
```bash
# Encontre o processo
netstat -ano | findstr :8080
# Mate o processo ou altere a porta no application.properties
```

### **Erro de permissão de upload**
```bash
# Crie o diretório manualmente
mkdir uploads/images
# Verifique permissões (Linux/macOS)
chmod 755 uploads/images
```

### **JAVA_HOME não configurado**
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17
# Linux/macOS
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

## 📊 **Dados de Demonstração**

O sistema é inicializado automaticamente com:
- 👤 **Usuários**: admin, testuser, e outros aventureiros
- ⚔️ **Produtos**: Armaduras, armas, poções e artefatos
- 💰 **Transações**: Histórico de compras e vendas
- 🏆 **Rankings**: Dados para demonstração

## 🚀 **Próximas Funcionalidades**

- 🔔 **Sistema de Notificações** - Alertas em tempo real
- 💬 **Chat entre Usuários** - Comunicação direta
- 🎯 **Sistema de Reputação** - Avaliações e reviews
- 📱 **API REST Completa** - Integração mobile
- 🌍 **Internacionalização** - Múltiplos idiomas

## 🚀 **Deploy no Render.com (GRATUITO)**

Este projeto está totalmente configurado para deploy automático no Render.com (plano gratuito).

### **1. Preparação**
```bash
# Clone o repositório
git clone <url-do-seu-repositorio>
cd rpg_market

# Certifique-se que os arquivos estão presentes:
# ✅ Dockerfile
# ✅ render.yaml  
# ✅ application-prod.properties
```

### **2. Deploy no Render**
1. 🌐 Acesse [render.com](https://render.com) e faça login/cadastro
2. 🔗 Conecte seu repositório GitHub
3. 📦 Crie um **Web Service** e selecione seu repositório
4. ⚙️ O Render detectará automaticamente o `render.yaml` e configurará tudo
5. 🔐 **IMPORTANTE**: Configure a variável `MONGODB_URI` no dashboard:
   - Vá em **Environment Variables**
   - Adicione: `MONGODB_URI` = `sua-connection-string-mongodb`
   - Exemplo: `mongodb+srv://user:password@cluster.mongodb.net/`

### **3. Configuração do MongoDB**
- 📊 Use **MongoDB Atlas** (gratuito): [cloud.mongodb.com](https://cloud.mongodb.com)
- 🔑 Crie um cluster gratuito e obtenha a connection string
- 🌐 Configure o IP `0.0.0.0/0` para acesso do Render

### **4. Variáveis Já Configuradas**
O `render.yaml` já inclui:
- ✅ `SPRING_PROFILES_ACTIVE=prod`
- ✅ `SERVER_PORT=8080`
- ✅ `APP_UPLOAD_DIR=/tmp/uploads`
- ✅ `MONGODB_DATABASE=Cluster0`
- ⚠️ `MONGODB_URI` (configure manualmente por segurança)

### **5. Primeiro Deploy**
- ⏱️ **Tempo**: ~5-10 minutos
- 🔄 **Auto-deploy**: Pushes na branch principal
- 📱 **URL**: `https://seu-app.onrender.com`

---

## 🤝 **Contribuição**

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit as mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 **Licença**

Este projeto está sob a licença MIT. Consulte o arquivo LICENSE para detalhes.

## 📞 **Suporte**

- 🐛 **Issues**: Reporte bugs no repositório
- 📖 **Documentação**: Spring Boot e MongoDB
- 📝 **Logs**: Consulte `logs/application.log`

---

⚔️ **Que suas aventuras sejam épicas, nobre guerreiro!** ⚔️
