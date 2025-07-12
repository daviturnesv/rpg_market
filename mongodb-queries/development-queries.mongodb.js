// MongoDB Playground - RPG Market Development
// Queries para desenvolvimento, testes e depuração

// Conectar ao banco de dados RPG Market
use('Cluster0');

// ====== QUERIES RÁPIDAS PARA DESENVOLVIMENTO ======

// 1. Verificar se tudo está funcionando
console.log("=== QUICK CHECK ===");
console.log("Users:", db.users.countDocuments());
console.log("Products:", db.products.countDocuments());
console.log("Transactions:", db.transactions.countDocuments());
console.log("Addresses:", db.deliveryAddresses.countDocuments());

// 2. Pegar exemplos de cada tipo de documento
console.log("\n=== SAMPLE DOCUMENTS ===");
console.log("Sample User:", db.users.findOne());
console.log("Sample Product:", db.products.findOne());
console.log("Sample Transaction:", db.transactions.findOne());
console.log("Sample Address:", db.deliveryAddresses.findOne());

// ====== QUERIES PARA TESTES ======

// 3. Buscar dados específicos para testes
const testUser = db.users.findOne({ userType: "AVENTUREIRO" });
console.log("Test User:", testUser);

const testProduct = db.products.findOne({ category: "ARMAS" });
console.log("Test Product:", testProduct);

// 4. Verificar relacionamentos
if (testUser) {
  console.log("\nEndereços do usuário teste:");
  db.deliveryAddresses.find({ user: testUser._id });
  
  console.log("\nProdutos do usuário teste:");
  db.products.find({ "seller._id": testUser._id });
  
  console.log("\nTransações como comprador:");
  db.transactions.find({ buyer: testUser._id });
}

// ====== QUERIES DE DEPURAÇÃO ======

// 5. Verificar produtos por categoria
console.log("\n=== PRODUTOS POR CATEGORIA ===");
const categories = ["ARMAS", "ARMADURA_VESTIMENTA", "POCOES", "INGREDIENTES_ALQUIMIA", "PERGAMINHOS"];
categories.forEach(category => {
  const count = db.products.countDocuments({ category: category });
  console.log(`${category}: ${count} produtos`);
});

// 6. Verificar transações por status
console.log("\n=== TRANSAÇÕES POR STATUS ===");
const statuses = ["PENDING", "COMPLETED", "CANCELLED"];
statuses.forEach(status => {
  const count = db.transactions.countDocuments({ status: status });
  console.log(`${status}: ${count} transações`);
});

// 7. Últimos documentos criados
console.log("\n=== ÚLTIMOS DOCUMENTOS CRIADOS ===");
console.log("Último usuário:", db.users.findOne({}, { sort: { createdAt: -1 } }));
console.log("Último produto:", db.products.findOne({}, { sort: { createdAt: -1 } }));
console.log("Última transação:", db.transactions.findOne({}, { sort: { createdAt: -1 } }));

// ====== SIMULAÇÃO DE QUERIES DA APLICAÇÃO ======

// 8. Simular busca de produtos para o marketplace
console.log("\n=== SIMULAÇÃO: BUSCA NO MARKETPLACE ===");
db.products.find({ 
  $and: [
    { $or: [{ sold: false }, { sold: { $exists: false } }] },
    { category: "ARMAS" }
  ]
}).sort({ createdAt: -1 }).limit(10);

// 9. Simular busca de produtos de um vendedor
console.log("\n=== SIMULAÇÃO: PRODUTOS DE UM VENDEDOR ===");
const sampleSeller = db.users.findOne({ userType: "AVENTUREIRO" });
if (sampleSeller) {
  db.products.find({ 
    "seller._id": sampleSeller._id 
  }).sort({ createdAt: -1 });
}

// 10. Simular histórico de transações de um usuário
console.log("\n=== SIMULAÇÃO: HISTÓRICO DE TRANSAÇÕES ===");
const sampleBuyer = db.users.findOne({ userType: "AVENTUREIRO" });
if (sampleBuyer) {
  db.transactions.find({ 
    $or: [
      { buyer: sampleBuyer._id },
      { seller: sampleBuyer._id }
    ]
  }).sort({ createdAt: -1 });
}

// ====== TESTES DE PERFORMANCE ======

// 11. Teste de busca com texto (simulação)
console.log("\n=== TESTE: BUSCA POR TEXTO ===");
db.products.find({ 
  name: { $regex: "espada|sword|blade", $options: "i" } 
}).limit(5);

// 12. Teste de agregação complexa
console.log("\n=== TESTE: AGREGAÇÃO COMPLEXA ===");
db.transactions.aggregate([
  {
    $lookup: {
      from: "products",
      localField: "product",
      foreignField: "_id",
      as: "productInfo"
    }
  },
  {
    $lookup: {
      from: "users",
      localField: "buyer",
      foreignField: "_id",
      as: "buyerInfo"
    }
  },
  {
    $unwind: "$productInfo"
  },
  {
    $unwind: "$buyerInfo"
  },
  {
    $project: {
      transactionId: "$_id",
      productName: "$productInfo.name",
      productCategory: "$productInfo.category",
      buyerUsername: "$buyerInfo.username",
      amount: "$price",
      status: "$status",
      date: "$createdAt"
    }
  },
  { $limit: 10 }
]);

// ====== VALIDAÇÃO DE ESTRUTURA DE DADOS ======

// 13. Validar estrutura dos usuários
console.log("\n=== VALIDAÇÃO: ESTRUTURA USUÁRIOS ===");
const userSample = db.users.findOne();
if (userSample) {
  const requiredUserFields = ['username', 'email', 'userType', 'createdAt'];
  const userFields = Object.keys(userSample);
  console.log("Campos encontrados:", userFields);
  console.log("Campos obrigatórios presentes:", 
    requiredUserFields.every(field => userFields.includes(field))
  );
}

// 14. Validar estrutura dos produtos
console.log("\n=== VALIDAÇÃO: ESTRUTURA PRODUTOS ===");
const productSample = db.products.findOne();
if (productSample) {
  const requiredProductFields = ['name', 'description', 'price', 'category', 'seller'];
  const productFields = Object.keys(productSample);
  console.log("Campos encontrados:", productFields);
  console.log("Campos obrigatórios presentes:", 
    requiredProductFields.every(field => productFields.includes(field))
  );
}

// 15. Validar estrutura das transações
console.log("\n=== VALIDAÇÃO: ESTRUTURA TRANSAÇÕES ===");
const transactionSample = db.transactions.findOne();
if (transactionSample) {
  const requiredTransactionFields = ['buyer', 'seller', 'product', 'price', 'status'];
  const transactionFields = Object.keys(transactionSample);
  console.log("Campos encontrados:", transactionFields);
  console.log("Campos obrigatórios presentes:", 
    requiredTransactionFields.every(field => transactionFields.includes(field))
  );
}

// ====== QUERIES PARA SEEDS/DEMO DATA ======

// 16. Verificar se dados de demonstração existem
console.log("\n=== VERIFICAÇÃO: DADOS DE DEMONSTRAÇÃO ===");
const hasUsers = db.users.countDocuments() > 0;
const hasProducts = db.products.countDocuments() > 0;
const hasTransactions = db.transactions.countDocuments() > 0;

console.log("Sistema possui dados:", {
  users: hasUsers,
  products: hasProducts,
  transactions: hasTransactions,
  ready: hasUsers && hasProducts && hasTransactions
});

// 17. Verificar qualidade dos dados de demonstração
console.log("\n=== QUALIDADE DOS DADOS ===");
const usersWithAddresses = db.users.aggregate([
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
      "addresses.0": { $exists: true }
    }
  },
  { $count: "usersWithAddresses" }
]);

const productsWithImages = db.products.countDocuments({
  imagePath: { $exists: true, $ne: null, $ne: "" }
});

console.log("Usuários com endereços:", usersWithAddresses);
console.log("Produtos com imagens:", productsWithImages);
