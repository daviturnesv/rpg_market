// MongoDB Playground - RPG Market Maintenance
// Queries para manutenção, limpeza e correção de dados

// Conectar ao banco de dados RPG Market
use('Cluster0');

// ====== VERIFICAÇÕES DE SAÚDE DO BANCO ======

// 1. Verificar conexão e contagem geral
console.log("=== STATUS DO BANCO DE DADOS ===");
const healthCheck = {
  users: db.users.countDocuments(),
  products: db.products.countDocuments(),
  transactions: db.transactions.countDocuments(),
  deliveryAddresses: db.deliveryAddresses.countDocuments(),
  timestamp: new Date()
};
console.log("Health Check:", healthCheck);

// 2. Verificar índices das coleções
console.log("\n=== ÍNDICES DAS COLEÇÕES ===");
console.log("Users indexes:", db.users.getIndexes());
console.log("Products indexes:", db.products.getIndexes());
console.log("Transactions indexes:", db.transactions.getIndexes());
console.log("DeliveryAddresses indexes:", db.deliveryAddresses.getIndexes());

// ====== DETECÇÃO DE PROBLEMAS ======

// 3. Usuários duplicados (mesmo email)
console.log("\n=== USUÁRIOS DUPLICADOS (EMAIL) ===");
db.users.aggregate([
  {
    $group: {
      _id: "$email",
      count: { $sum: 1 },
      users: { $push: "$_id" }
    }
  },
  {
    $match: {
      count: { $gt: 1 }
    }
  }
]);

// 4. Usuários duplicados (mesmo username)
console.log("\n=== USUÁRIOS DUPLICADOS (USERNAME) ===");
db.users.aggregate([
  {
    $group: {
      _id: "$username",
      count: { $sum: 1 },
      users: { $push: "$_id" }
    }
  },
  {
    $match: {
      count: { $gt: 1 }
    }
  }
]);

// 5. Produtos sem preço ou preço inválido
console.log("\n=== PRODUTOS COM PREÇO INVÁLIDO ===");
db.products.find({
  $or: [
    { price: null },
    { price: { $exists: false } },
    { price: { $lte: 0 } },
    { price: { $type: "string" } }
  ]
});

// 6. Transações com valores inconsistentes
console.log("\n=== TRANSAÇÕES COM VALORES INCONSISTENTES ===");
db.transactions.find({
  $or: [
    { price: null },
    { price: { $exists: false } },
    { price: { $lte: 0 } }
  ]
});

// 7. Produtos órfãos (sem vendedor)
console.log("\n=== PRODUTOS ÓRFÃOS ===");
db.products.find({
  $or: [
    { seller: null },
    { seller: { $exists: false } },
    { "seller._id": null },
    { "seller._id": { $exists: false } }
  ]
});

// 8. Endereços órfãos (usuário não existe)
console.log("\n=== ENDEREÇOS ÓRFÃOS ===");
db.deliveryAddresses.aggregate([
  {
    $lookup: {
      from: "users",
      localField: "user",
      foreignField: "_id",
      as: "userExists"
    }
  },
  {
    $match: {
      "userExists": { $size: 0 }
    }
  }
]);

// 9. Transações órfãs (produto ou usuário não existe)
console.log("\n=== TRANSAÇÕES ÓRFÃS ===");
db.transactions.aggregate([
  {
    $lookup: {
      from: "products",
      localField: "product",
      foreignField: "_id",
      as: "productExists"
    }
  },
  {
    $lookup: {
      from: "users",
      localField: "buyer",
      foreignField: "_id",
      as: "buyerExists"
    }
  },
  {
    $match: {
      $or: [
        { "productExists": { $size: 0 } },
        { "buyerExists": { $size: 0 } }
      ]
    }
  }
]);

// ====== LIMPEZA DE DADOS (USAR COM CUIDADO!) ======

// 10. Remover produtos sem preço válido (DESCOMENTEAR PARA USAR)
/*
db.products.deleteMany({
  $or: [
    { price: null },
    { price: { $exists: false } },
    { price: { $lte: 0 } }
  ]
});
*/

// 11. Remover endereços órfãos (DESCOMENTEAR PARA USAR)
/*
const orphanAddresses = db.deliveryAddresses.aggregate([
  {
    $lookup: {
      from: "users",
      localField: "user",
      foreignField: "_id",
      as: "userExists"
    }
  },
  {
    $match: {
      "userExists": { $size: 0 }
    }
  },
  {
    $project: { _id: 1 }
  }
]);

const orphanIds = orphanAddresses.map(doc => doc._id);
db.deliveryAddresses.deleteMany({ _id: { $in: orphanIds } });
*/

// 12. Limpar TODOS os dados (EMERGENCY RESET - MUITO CUIDADO!)
/*
console.log("ATENÇÃO: Limpeza completa do banco!");
db.users.deleteMany({});
db.products.deleteMany({});
db.transactions.deleteMany({});
db.deliveryAddresses.deleteMany({});
console.log("Todos os dados foram removidos!");
*/

// ====== CORREÇÕES DE DADOS ======

// 13. Corrigir produtos marcados como vendidos mas sem transação
console.log("\n=== PRODUTOS MARCADOS COMO VENDIDOS SEM TRANSAÇÃO ===");
db.products.aggregate([
  {
    $match: { sold: true }
  },
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
  }
]);

// 14. Corrigir status sold dos produtos (DESCOMENTEAR PARA USAR)
/*
// Marcar como vendidos os produtos que têm transações completed
const soldProducts = db.transactions.distinct("product", { status: "COMPLETED" });
db.products.updateMany(
  { _id: { $in: soldProducts } },
  { $set: { sold: true } }
);

// Marcar como não vendidos os produtos sem transações completed
db.products.updateMany(
  { _id: { $nin: soldProducts } },
  { $set: { sold: false } }
);
*/

// ====== BACKUP E ESTATÍSTICAS ======

// 15. Gerar relatório de backup
console.log("\n=== RELATÓRIO DE BACKUP ===");
const backupReport = {
  timestamp: new Date(),
  collections: {
    users: {
      count: db.users.countDocuments(),
      sampleDoc: db.users.findOne()
    },
    products: {
      count: db.products.countDocuments(),
      sampleDoc: db.products.findOne()
    },
    transactions: {
      count: db.transactions.countDocuments(),
      sampleDoc: db.transactions.findOne()
    },
    deliveryAddresses: {
      count: db.deliveryAddresses.countDocuments(),
      sampleDoc: db.deliveryAddresses.findOne()
    }
  }
};
console.log("Backup Report:", JSON.stringify(backupReport, null, 2));

// 16. Estatísticas de uso do banco
console.log("\n=== ESTATÍSTICAS DE USO ===");
db.stats();

// 17. Verificar tamanho das coleções
console.log("\n=== TAMANHO DAS COLEÇÕES ===");
console.log("Users collection stats:", db.users.stats());
console.log("Products collection stats:", db.products.stats());
console.log("Transactions collection stats:", db.transactions.stats());
console.log("DeliveryAddresses collection stats:", db.deliveryAddresses.stats());
