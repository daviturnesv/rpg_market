// MongoDB Playground - RPG Market Admin
// Queries administrativas e de gestão

// Conectar ao banco de dados RPG Market
use('Cluster0');

// ====== GESTÃO DE USUÁRIOS ======

// 1. Listar todos os usuários com informações completas
console.log("=== TODOS OS USUÁRIOS ===");
db.users.find({}).sort({ createdAt: -1 }).toArray();

// 2. Usuários por tipo e status
console.log("\n=== USUÁRIOS POR TIPO ===");
db.users.find({ userType: "AVENTUREIRO" }).limit(10).toArray();
db.users.find({ userType: "MESTRE" }).limit(10).toArray();

// 3. Buscar usuário específico
// db.users.findOne({ username: "nomeUsuario" });

// 4. Usuários sem endereços cadastrados
console.log("\n=== USUÁRIOS SEM ENDEREÇOS ===");
db.users.aggregate([
  {
    $lookup: {
      from: "deliveryAddresses",
      localField: "_id",
      foreignField: "user",
      as: "addresses"
    }
  },
  {
    $match: {
      "addresses": { $size: 0 }
    }
  },
  {
    $project: {
      username: 1,
      email: 1,
      userType: 1,
      createdAt: 1
    }
  }
]);

// 5. Usuários mais ativos (com mais transações)
console.log("\n=== USUÁRIOS MAIS ATIVOS ===");
db.transactions.aggregate([
  {
    $group: {
      _id: "$buyer",
      purchaseCount: { $sum: 1 },
      totalSpent: { $sum: "$price" }
    }
  },
  { $sort: { purchaseCount: -1 } },
  { $limit: 20 }
]);

// ====== GESTÃO DE PRODUTOS ======

// 6. Produtos por categoria
console.log("\n=== PRODUTOS POR CATEGORIA ===");
db.products.find({ category: "ARMAS" }).sort({ price: -1 }).toArray();
db.products.find({ category: "ARMADURA_VESTIMENTA" }).sort({ price: -1 }).toArray();
db.products.find({ category: "POCOES" }).sort({ price: -1 }).toArray();
db.products.find({ category: "INGREDIENTES_ALQUIMIA" }).sort({ price: -1 }).toArray();
db.products.find({ category: "PERGAMINHOS" }).sort({ price: -1 }).toArray();

// 7. Produtos sem imagem
console.log("\n=== PRODUTOS SEM IMAGEM ===");
db.products.find({ 
  $or: [
    { imagePath: null },
    { imagePath: "" },
    { imagePath: { $exists: false } }
  ]
}).sort({ createdAt: -1 }).toArray();

// 8. Produtos disponíveis para venda
console.log("\n=== PRODUTOS DISPONÍVEIS ===");
db.products.find({ 
  $or: [
    { sold: false },
    { sold: { $exists: false } }
  ]
}).sort({ createdAt: -1 }).toArray();

// 9. Produtos vendidos
console.log("\n=== PRODUTOS VENDIDOS ===");
db.products.find({ sold: true }).sort({ createdAt: -1 }).toArray();

// 10. Buscar produto por nome (exemplo)
// db.products.find({ name: { $regex: "espada", $options: "i" } });

// ====== GESTÃO DE TRANSAÇÕES ======

// 11. Transações por status
console.log("\n=== TRANSAÇÕES POR STATUS ===");
db.transactions.find({ status: "PENDING" }).sort({ createdAt: -1 }).toArray();
db.transactions.find({ status: "COMPLETED" }).sort({ createdAt: -1 }).toArray();
db.transactions.find({ status: "CANCELLED" }).sort({ createdAt: -1 }).toArray();

// 12. Transações recentes (últimas 24h)
console.log("\n=== TRANSAÇÕES ÚLTIMAS 24H ===");
const yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
db.transactions.find({ 
  createdAt: { $gte: yesterday } 
}).sort({ createdAt: -1 }).toArray();

// 13. Transações de alto valor (acima de $100)
console.log("\n=== TRANSAÇÕES DE ALTO VALOR ===");
db.transactions.find({ price: { $gte: 100 } }).sort({ price: -1 }).toArray();

// 14. Transações sem endereço de entrega
console.log("\n=== TRANSAÇÕES SEM ENDEREÇO ===");
db.transactions.find({ 
  deliveryAddress: { $exists: false }
}).toArray();

// ====== OPERAÇÕES DE USUÁRIOS ESPECÍFICOS ======

// 15. Ver todas as transações de um usuário específico
// Exemplo: substitua o ObjectId pelo ID real do usuário
function getUserTransactions(userId) {
  return db.transactions.find({ 
    $or: [
      { buyer: ObjectId(userId) },
      { seller: ObjectId(userId) }
    ]
  }).sort({ createdAt: -1 });
}

// 16. Ver todos os produtos de um vendedor específico
// Exemplo: substitua o ObjectId pelo ID real do usuário
function getUserProducts(userId) {
  return db.products.find({ 
    "seller._id": ObjectId(userId) 
  }).sort({ createdAt: -1 });
}

// 17. Ver endereços de um usuário específico
// Exemplo: substitua o ObjectId pelo ID real do usuário
function getUserAddresses(userId) {
  return db.deliveryAddresses.find({ 
    user: ObjectId(userId) 
  });
}

// ====== VERIFICAÇÕES DE INTEGRIDADE ======

// 18. Produtos órfãos (sem vendedor válido)
console.log("\n=== PRODUTOS ÓRFÃOS ===");
db.products.find({ 
  $or: [
    { "seller._id": null },
    { "seller._id": { $exists: false } },
    { seller: null },
    { seller: { $exists: false } }
  ]
}).toArray();

// 19. Transações órfãs (sem produto ou usuário válido)
console.log("\n=== TRANSAÇÕES ÓRFÃS ===");
db.transactions.find({ 
  $or: [
    { product: null },
    { product: { $exists: false } },
    { buyer: null },
    { buyer: { $exists: false } },
    { seller: null },
    { seller: { $exists: false } }
  ]
}).toArray();

// 20. Contar documentos em todas as coleções
console.log("\n=== CONTAGEM DE DOCUMENTOS ===");
console.log("Usuários:", db.users.countDocuments());
console.log("Produtos:", db.products.countDocuments());
console.log("Transações:", db.transactions.countDocuments());
console.log("Endereços:", db.deliveryAddresses.countDocuments());
