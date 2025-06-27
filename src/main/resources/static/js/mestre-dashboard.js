/**
 * Script para dashboard de análises dos mestres
 * Contém funcionalidades de atualização automática e interação com gráficos
 */

// Variáveis globais para controle dos gráficos
let salesChart, categoriesChart, statusChart, sellersChart;
let autoRefreshEnabled = true;
let refreshInterval;

// Configuração de cores para gráficos
const chartColors = {
    primary: '#007bff',
    success: '#28a745',
    warning: '#ffc107',
    danger: '#dc3545',
    info: '#17a2b8',
    gold: '#d4af37',
    purple: '#6f42c1',
    orange: '#fd7e14'
};

/**
 * Inicializa o dashboard quando a página carrega
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🎮 Inicializando Painel do Mestre...');
    
    // Inicializar gráficos
    initializeCharts();
    
    // Configurar auto-refresh
    setupAutoRefresh();
    
    // Configurar eventos dos botões
    setupEventListeners();
    
    console.log('✅ Painel do Mestre inicializado com sucesso!');
});

/**
 * Inicializa todos os gráficos do dashboard
 */
function initializeCharts() {
    try {
        // Gráfico de vendas por dia
        initSalesChart();
        
        // Gráfico de vendas por categoria
        initCategoriesChart();
        
        // Gráfico de status das transações
        initStatusChart();
        
        // Gráfico de top vendedores
        initSellersChart();
        
    } catch (error) {
        console.error('❌ Erro ao inicializar gráficos:', error);
        showErrorMessage('Erro ao carregar gráficos. Recarregando página...');
        setTimeout(() => window.location.reload(), 3000);
    }
}

/**
 * Inicializa o gráfico de vendas por dia
 */
function initSalesChart() {
    const ctx = document.getElementById('vendasPorDiaChart');
    if (!ctx) return;
    
    salesChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [], // Será preenchido via AJAX
            datasets: [{
                label: 'Vendas (Moedas de Ouro)',
                data: [],
                borderColor: chartColors.gold,
                backgroundColor: chartColors.gold + '20',
                borderWidth: 3,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Vendas dos Últimos 30 Dias',
                    font: { size: 16, weight: 'bold' }
                },
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return value + ' 🪙';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Inicializa o gráfico de vendas por categoria
 */
function initCategoriesChart() {
    const ctx = document.getElementById('categoriaChart');
    if (!ctx) return;
    
    categoriesChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: [
                    chartColors.primary,
                    chartColors.success,
                    chartColors.warning,
                    chartColors.danger,
                    chartColors.info,
                    chartColors.purple,
                    chartColors.orange
                ],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Vendas por Categoria',
                    font: { size: 16, weight: 'bold' }
                },
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

/**
 * Inicializa o gráfico de status das transações
 */
function initStatusChart() {
    const ctx = document.getElementById('statusChart');
    if (!ctx) return;
    
    statusChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Quantidade',
                data: [],
                backgroundColor: [
                    chartColors.success,
                    chartColors.warning,
                    chartColors.danger,
                    chartColors.info
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Status das Transações',
                    font: { size: 16, weight: 'bold' }
                },
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

/**
 * Inicializa o gráfico de top vendedores
 */
function initSellersChart() {
    const ctx = document.getElementById('vendedoresChart');
    if (!ctx) return;
    
    sellersChart = new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: [],
            datasets: [{
                label: 'Vendas (Moedas)',
                data: [],
                backgroundColor: chartColors.gold + '80',
                borderColor: chartColors.gold,
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Top 5 Vendedores',
                    font: { size: 16, weight: 'bold' }
                },
                legend: {
                    display: false
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return value + ' 🪙';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Atualiza todos os gráficos com dados do servidor
 */
async function updateChartsData() {
    try {
        console.log('🔄 Atualizando dados dos gráficos...');
        
        const response = await fetch('/mestre/api/dados-graficos');
        if (!response.ok) {
            throw new Error(`Erro HTTP! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Atualizar cada gráfico
        updateSalesChart(data.vendasPorDia || []);
        updateCategoriesChart(data.vendasPorCategoria || []);
        updateStatusChart(data.transacoesPorStatus || []);
        updateSellersChart(data.topVendedores || []);
        
        // Atualizar cards de métricas
        updateMetricsCards(data);
        
        console.log('✅ Gráficos atualizados com sucesso!');
        
    } catch (error) {
        console.error('❌ Erro ao atualizar gráficos:', error);
        showErrorMessage('Erro ao atualizar dados. Tentando novamente...');
    }
}

/**
 * Atualiza o gráfico de vendas por dia
 */
function updateSalesChart(vendasData) {
    if (!salesChart || !vendasData) return;
    
    const labels = vendasData.map(v => formatDate(v.data));
    const data = vendasData.map(v => parseFloat(v.valor) || 0);
    
    salesChart.data.labels = labels;
    salesChart.data.datasets[0].data = data;
    salesChart.update();
}

/**
 * Atualiza o gráfico de categorias
 */
function updateCategoriesChart(categoriaData) {
    if (!categoriesChart || !categoriaData) return;
    
    const labels = categoriaData.map(c => c.categoria);
    const data = categoriaData.map(c => c.quantidade || 0);
    
    categoriesChart.data.labels = labels;
    categoriesChart.data.datasets[0].data = data;
    categoriesChart.update();
}

/**
 * Atualiza o gráfico de status
 */
function updateStatusChart(statusData) {
    if (!statusChart || !statusData) return;
    
    const labels = statusData.map(s => s.status);
    const data = statusData.map(s => s.quantidade || 0);
    
    statusChart.data.labels = labels;
    statusChart.data.datasets[0].data = data;
    statusChart.update();
}

/**
 * Atualiza o gráfico de vendedores
 */
function updateSellersChart(vendedoresData) {
    if (!sellersChart || !vendedoresData) return;
    
    const topVendedores = vendedoresData.slice(0, 5);
    const labels = topVendedores.map(v => v.username);
    const data = topVendedores.map(v => parseFloat(v.valorTotal) || 0);
    
    sellersChart.data.labels = labels;
    sellersChart.data.datasets[0].data = data;
    sellersChart.update();
}

/**
 * Atualiza os cards de métricas
 */
function updateMetricsCards(data) {
    // Atualizar valores nos cards
    updateCardValue('total-usuarios', data.totalUsuarios || 0);
    updateCardValue('total-produtos', data.totalProdutos || 0);
    updateCardValue('total-transacoes', data.totalTransacoes || 0);
    updateCardValue('volume-vendas', formatCurrency(data.volumeTotalVendas || 0));
}

/**
 * Atualiza o valor de um card específico
 */
function updateCardValue(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
        // Adicionar animação de destaque
        element.parentElement.classList.add('highlight-update');
        setTimeout(() => {
            element.parentElement.classList.remove('highlight-update');
        }, 1000);
    }
}

/**
 * Configura o sistema de auto-refresh
 */
function setupAutoRefresh() {
    // Atualizar dados imediatamente
    updateChartsData();
    
    // Configurar intervalo de atualização (5 minutos)
    refreshInterval = setInterval(() => {
        if (autoRefreshEnabled) {
            updateChartsData();
        }
    }, 300000); // 5 minutos
    
    console.log('🔄 Auto-refresh configurado para 5 minutos');
}

/**
 * Configura event listeners para botões e controles
 */
function setupEventListeners() {
    // Botão de refresh manual
    const refreshBtn = document.getElementById('refresh-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            this.disabled = true;
            this.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Atualizando...';
            
            updateChartsData().finally(() => {
                this.disabled = false;
                this.innerHTML = '<i class="fas fa-sync-alt me-2"></i>Atualizar Dados';
            });
        });
    }
    
    // Toggle auto-refresh
    const autoRefreshToggle = document.getElementById('auto-refresh-toggle');
    if (autoRefreshToggle) {
        autoRefreshToggle.addEventListener('change', function() {
            autoRefreshEnabled = this.checked;
            console.log('Auto-refresh:', autoRefreshEnabled ? 'Ativado' : 'Desativado');
        });
    }
}

/**
 * Exibe mensagem de erro
 */
function showErrorMessage(message) {
    // Criar toast de erro se não existir
    const toast = document.createElement('div');
    toast.className = 'toast align-items-center text-white bg-danger border-0 position-fixed';
    toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999;';
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="fas fa-exclamation-triangle me-2"></i>${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    const bsToast = new bootstrap.Toast(toast, { delay: 5000 });
    bsToast.show();
    
    // Remover do DOM após esconder
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

/**
 * Formata data para exibição
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', { 
        day: '2-digit', 
        month: '2-digit' 
    });
}

/**
 * Formata valor monetário
 */
function formatCurrency(value) {
    return parseFloat(value || 0).toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

/**
 * Cleanup ao sair da página
 */
window.addEventListener('beforeunload', function() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
});
