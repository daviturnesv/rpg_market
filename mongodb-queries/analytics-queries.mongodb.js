// MongoDB Playground - RPG Market Analytics
// Queries para análise de dados e relatórios

// Conectar ao banco de dados RPG Market
use('Cluster0');

// ====== DASHBOARD ANALYTICS ======

// 1. Estatísticas gerais do sistema
console.log("=== ESTATÍSTICAS GERAIS ===");
const stats = {
  totalUsers: db.users.countDocuments(),
  totalProducts: db.products.countDocuments(),
  totalTransactions: db.transactions.countDocuments(),
  totalAddresses: db.deliveryAddresses.countDocuments()
};
console.log(stats);

// 2. Análise de vendas por status
console.log("\n=== VENDAS POR STATUS ===");
db.transactions.aggregate([
  {
    $group: {
      _id: "$status",
      count: { $sum: 1 },
      totalValue: { $sum: "$price" },
      avgValue: { $avg: "$price" }
    }
  },
  { $sort: { totalValue: -1 } }
]);

// 3. Top 10 vendedores por receita
console.log("\n=== TOP VENDEDORES ===");
db.transactions.aggregate([
  {
    $group: {
      _id: "$seller",
      salesCount: { $sum: 1 },
      totalRevenue: { $sum: "$price" },
      avgSaleValue: { $avg: "$price" }
    }
  },
  { $sort: { totalRevenue: -1 } },
  { $limit: 10 }
]);

// 4. Produtos mais vendidos por categoria
console.log("\n=== PRODUTOS MAIS VENDIDOS POR CATEGORIA ===");
db.transactions.aggregate([
  {
    $lookup: {
      from: "products",
      localField: "product",
      foreignField: "_id",
      as: "productInfo"
    }
  },
  { $unwind: "$productInfo" },
  {
    $group: {
      _id: "$productInfo.category",
      salesCount: { $sum: 1 },
      totalRevenue: { $sum: "$price" },
      avgPrice: { $avg: "$price" }
    }
  },
  { $sort: { salesCount: -1 } }
]);

// 5. Análise temporal de vendas (últimos 30 dias)
console.log("\n=== VENDAS ÚLTIMOS 30 DIAS ===");
const thirtyDaysAgo = new Date();
thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

db.transactions.aggregate([
  {
    $match: {
      createdAt: { $gte: thirtyDaysAgo }
    }
  },
  {
    $group: {
      _id: {
        year: { $year: "$createdAt" },
        month: { $month: "$createdAt" },
        day: { $dayOfMonth: "$createdAt" }
      },
      dailySales: { $sum: 1 },
      dailyRevenue: { $sum: "$price" }
    }
  },
  { $sort: { "_id.year": 1, "_id.month": 1, "_id.day": 1 } }
]);

// 6. Ranking de produtos por preço
console.log("\n=== PRODUTOS MAIS CAROS ===");
db.products.find({}).sort({ price: -1 }).limit(10);

// 7. Distribuição de usuários por tipo
console.log("\n=== DISTRIBUIÇÃO DE USUÁRIOS ===");
db.users.aggregate([
  {
    $group: {
      _id: "$userType",
      count: { $sum: 1 }
    }
  },
  { $sort: { count: -1 } }
]);

// 8. Taxa de conversão (produtos listados vs vendidos)
console.log("\n=== TAXA DE CONVERSÃO ===");
const conversionStats = db.products.aggregate([
  {
    $group: {
      _id: null,
      totalProducts: { $sum: 1 },
      soldProducts: {
        $sum: {
          $cond: [{ $eq: ["$sold", true] }, 1, 0]
        }
      }
    }
  },
  {
    $project: {
      _id: 0,
      totalProducts: 1,
      soldProducts: 1,
      conversionRate: {
        $multiply: [
          { $divide: ["$soldProducts", "$totalProducts"] },
          100
        ]
      }
    }
  }
]);

// 9. Valor médio de transação por categoria
console.log("\n=== VALOR MÉDIO POR CATEGORIA ===");
db.transactions.aggregate([
  {
    $lookup: {
      from: "products",
      localField: "product",
      foreignField: "_id",
      as: "productInfo"
    }
  },
  { $unwind: "$productInfo" },
  {
    $group: {
      _id: "$productInfo.category",
      avgTransactionValue: { $avg: "$price" },
      minValue: { $min: "$price" },
      maxValue: { $max: "$price" },
      transactionCount: { $sum: 1 }
    }
  },
  { $sort: { avgTransactionValue: -1 } }
]);

// 10. Produtos sem transações (não vendidos)
console.log("\n=== PRODUTOS SEM VENDAS ===");
db.products.aggregate([
  {
    $lookup: {
      from: "transactions",
      localField: "_id",
      foreignField: "product",
      as: "transactions"
    }
  },
  {
    $match: {
      "transactions": { $size: 0 }
    }
  },
  {
    $project: {
      name: 1,
      category: 1,
      price: 1,
      createdAt: 1
    }
  },
  { $sort: { createdAt: -1 } }
]);
