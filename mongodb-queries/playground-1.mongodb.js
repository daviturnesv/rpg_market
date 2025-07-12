/* global use, db */
// MongoDB Playground
// To disable this template go to Settings | MongoDB | Use Default Template For Playground.
// Make sure you are connected to enable completions and to be able to run a playground.
// Use Ctrl+Space inside a snippet or a string literal to trigger completions.
// The result of the last command run in a playground is shown on the results panel.
// By default the first 20 documents will be returned with a cursor.
// Use 'console.log()' to print to the debug output.
// For more documentation on playgrounds please refer to
// https://www.mongodb.com/docs/mongodb-vscode/playgrounds/

// RPG Market - MongoDB Playground
// Conectar ao banco de dados do RPG Market
use('Cluster0');

// ===== CONSULTAS DE EXEMPLO PARA O RPG MARKET =====

// 1. Ver todos os usuários do sistema
console.log("=== USUÁRIOS DO SISTEMA ===");
db.users.find().limit(5).forEach(user => {
    console.log(`Username: ${user.username}, Role: ${user.role}, Level: ${user.level || 'N/A'}`);
});

// 2. Ver produtos disponíveis para venda
console.log("\n=== PRODUTOS DISPONÍVEIS ===");
db.products.find({ status: "AVAILABLE" }).limit(5).forEach(product => {
    console.log(`${product.name} - Preço: $${product.price} - Categoria: ${product.category}`);
});

// 3. Contar produtos por categoria
console.log("\n=== PRODUTOS POR CATEGORIA ===");
db.products.aggregate([
    { $group: { _id: "$category", count: { $sum: 1 } } },
    { $sort: { count: -1 } }
]).forEach(result => {
    console.log(`${result._id}: ${result.count} produtos`);
});

// 4. Ver transações recentes
console.log("\n=== TRANSAÇÕES RECENTES ===");
db.transactions.find().sort({ createdAt: -1 }).limit(5).forEach(transaction => {
    console.log(`Status: ${transaction.status}, Valor: $${transaction.amount}`);
});

// 5. Top vendedores (usuários com mais produtos)
console.log("\n=== TOP VENDEDORES ===");
db.products.aggregate([
    { $group: { _id: "$seller.username", totalProducts: { $sum: 1 } } },
    { $sort: { totalProducts: -1 } },
    { $limit: 5 }
]).forEach(seller => {
    console.log(`${seller._id}: ${seller.totalProducts} produtos`);
});

// 6. Estatísticas gerais do sistema
console.log("\n=== ESTATÍSTICAS GERAIS ===");
const totalUsers = db.users.countDocuments();
const totalProducts = db.products.countDocuments();
const totalTransactions = db.transactions.countDocuments();

console.log(`Total de usuários: ${totalUsers}`);
console.log(`Total de produtos: ${totalProducts}`);
console.log(`Total de transações: ${totalTransactions}`);
