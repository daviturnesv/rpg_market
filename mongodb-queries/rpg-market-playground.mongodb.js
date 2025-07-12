// MongoDB Playground - RPG Market
// Use Ctrl+Shift+P e digite "MongoDB: Run Selected Lines in Playground" para executar queries

// Conectar ao banco de dados RPG Market
use('Cluster0');

// ====== EXEMPLOS DE QUERIES ÚTEIS ======

// 1. Verificar conexão - contar documentos em cada coleção
db.users.countDocuments();
db.products.countDocuments();
db.transactions.countDocuments();
db.deliveryAddresses.countDocuments();

// 2. Listar todos os usuários
db.users.find({}).limit(5);

// 3. Encontrar usuários por tipo
db.users.find({ userType: "AVENTUREIRO" }).limit(3);
db.users.find({ userType: "MESTRE" }).limit(3);

// 4. Listar produtos por categoria
db.products.find({ category: "ARMADURA_VESTIMENTA" }).limit(3);
db.products.find({ category: "ARMAS" }).limit(3);

// 5. Produtos disponíveis (não vendidos)
db.products.find({ 
  $or: [
    { sold: false },
    { sold: { $exists: false } }
  ]
}).limit(5);

// 6. Transações recentes
db.transactions.find({}).sort({ createdAt: -1 }).limit(5);

// 7. Transações por status
db.transactions.find({ status: "COMPLETED" }).limit(3);
db.transactions.find({ status: "PENDING" }).limit(3);

// 8. Análise de vendas - agrupar por status
db.transactions.aggregate([
  {
    $group: {
      _id: "$status",
      count: { $sum: 1 },
      totalValue: { $sum: "$price" }
    }
  }
]);

// 9. Top produtos mais caros
db.products.find({}).sort({ price: -1 }).limit(5);

// 10. Usuários com mais transações como vendedor
db.transactions.aggregate([
  {
    $group: {
      _id: "$seller",
      salesCount: { $sum: 1 },
      totalRevenue: { $sum: "$price" }
    }
  },
  { $sort: { salesCount: -1 } },
  { $limit: 5 }
]);

// ====== QUERIES PARA DEPURAÇÃO ======

// Verificar produtos sem imagem
db.products.find({ 
  $or: [
    { imagePath: null },
    { imagePath: "" },
    { imagePath: { $exists: false } }
  ]
});

// Verificar transações sem endereço de entrega
db.transactions.find({ 
  deliveryAddress: { $exists: false }
});

// Verificar usuários sem endereços
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
  }
]);

// ====== QUERIES DE MANUTENÇÃO ======

// Limpar dados de teste (CUIDADO!)
// db.users.deleteMany({});
// db.products.deleteMany({});
// db.transactions.deleteMany({});
// db.deliveryAddresses.deleteMany({});
