/* global use, db */
// MongoDB Playground - RPG Market Quick Admin
// Queries rápidas e diretas para administração

// Conectar ao banco de dados RPG Market
use('Cluster0');

// ====== VERIFICAÇÕES RÁPIDAS ======

// 1. Status geral do sistema
db.users.countDocuments();
db.products.countDocuments();
db.transactions.countDocuments();
db.deliveryAddresses.countDocuments();

// 2. Últimos 5 usuários criados
db.users.find({}).sort({ createdAt: -1 }).limit(5);

// 3. Últimos 5 produtos criados
db.products.find({}).sort({ createdAt: -1 }).limit(5);

// 4. Últimas 5 transações
db.transactions.find({}).sort({ createdAt: -1 }).limit(5);

// 5. Produtos disponíveis para venda
db.products.find({ 
  $or: [
    { sold: false },
    { sold: { $exists: false } }
  ]
}).limit(10);

// 6. Produtos vendidos
db.products.find({ sold: true }).limit(10);

// 7. Transações pendentes
db.transactions.find({ status: "PENDING" });

// 8. Transações completadas
db.transactions.find({ status: "COMPLETED" }).limit(10);

// 9. Usuários por tipo
db.users.aggregate([
  {
    $group: {
      _id: "$userType",
      count: { $sum: 1 }
    }
  }
]);

// 10. Produtos por categoria
db.products.aggregate([
  {
    $group: {
      _id: "$category",
      count: { $sum: 1 },
      avgPrice: { $avg: "$price" }
    }
  },
  { $sort: { count: -1 } }
]);

// 11. Top 5 produtos mais caros
db.products.find({}).sort({ price: -1 }).limit(5);

// 12. Top 5 vendedores (por quantidade de transações)
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

// 13. Usuários sem endereços
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
      userType: 1
    }
  }
]);

// 14. Produtos sem imagem
db.products.find({ 
  $or: [
    { imagePath: null },
    { imagePath: "" },
    { imagePath: { $exists: false } }
  ]
}).limit(10);

// 15. Buscar um usuário específico (exemplo)
// db.users.findOne({ username: "aventureiro1" });

// 16. Buscar produtos de uma categoria
// db.products.find({ category: "ARMAS" }).limit(5);

// 17. Buscar transações de um valor específico
// db.transactions.find({ price: { $gte: 50 } }).limit(5);
