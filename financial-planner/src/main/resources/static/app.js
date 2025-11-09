const API_BASE_URL = 'http://localhost:8081/api/v1';

// Category colors for charts
const CATEGORY_COLORS = {
    FOOD: '#ef4444',
    TRAVEL: '#3b82f6',
    BILLS: '#f59e0b',
    ENTERTAINMENT: '#8b5cf6',
    SHOPPING: '#ec4899',
    HEALTH: '#10b981',
    TRANSPORT: '#6366f1',
    EDUCATION: '#14b8a6',
    GROCERIES: '#84cc16',
    OTHER: '#6b7280'
};

// Category icons
const CATEGORY_ICONS = {
    FOOD: '🍔',
    TRAVEL: '✈️',
    BILLS: '📄',
    ENTERTAINMENT: '🎬',
    SHOPPING: '🛍️',
    HEALTH: '💊',
    TRANSPORT: '🚗',
    EDUCATION: '📚',
    GROCERIES: '🛒',
    OTHER: '📦'
};

let categoryChart = null;
let barChart = null;

// Load dashboard data on page load
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    document.getElementById('transactionForm').addEventListener('submit', addTransaction);
}

// Load complete dashboard
async function loadDashboard() {
    showLoading(true);
    
    try {
        const periodValue = document.getElementById('period').value;
        const [period, unit] = periodValue.split('-');
        
        const response = await fetch(`${API_BASE_URL}/dashboard?period=${period}&unit=${unit}`);
        
        if (!response.ok) {
            throw new Error('Failed to load dashboard');
        }
        
        const data = await response.json();
        
        // Update summary cards
        updateSummaryCards(data.analysis);
        
        // Update charts
        updateCharts(data.analysis);
        
        // Update recommendations
        updateRecommendations(data.recommendations);
        
        // Update category table
        updateCategoryTable(data.analysis.categoryBreakdown);
        
        // Update recent transactions
        updateRecentTransactions(data.recentTransactions);
        
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showToast('Failed to load dashboard data', 'error');
    } finally {
        showLoading(false);
    }
}

// Update summary cards
function updateSummaryCards(analysis) {
    document.getElementById('totalExpenses').textContent = 
        `$${analysis.totalExpenses.toFixed(2)}`;
    
    document.getElementById('totalIncome').textContent = 
        `$${analysis.totalIncome.toFixed(2)}`;
    
    const savingsRate = analysis.savingsRate.toFixed(1);
    const savingsElement = document.getElementById('savingsRate');
    savingsElement.textContent = `${savingsRate}%`;
    savingsElement.style.color = savingsRate >= 20 ? '#10b981' : '#ef4444';
    
    const trend = analysis.trendPercentage.toFixed(1);
    const trendElement = document.getElementById('trend');
    trendElement.textContent = `${trend > 0 ? '+' : ''}${trend}%`;
    trendElement.style.color = trend > 0 ? '#ef4444' : '#10b981';
}

// Update charts
function updateCharts(analysis) {
    // Prepare data for pie chart
    const labels = analysis.categoryBreakdown.map(cat => cat.categoryDisplayName);
    const amounts = analysis.categoryBreakdown.map(cat => cat.totalAmount);
    const colors = analysis.categoryBreakdown.map(cat => CATEGORY_COLORS[cat.category] || '#6b7280');
    
    // Destroy existing charts
    if (categoryChart) {
        categoryChart.destroy();
    }
    if (barChart) {
        barChart.destroy();
    }
    
    // Create pie chart
    const pieCtx = document.getElementById('categoryChart').getContext('2d');
    categoryChart = new Chart(pieCtx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: amounts,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            const percentage = analysis.categoryBreakdown[context.dataIndex].percentage;
                            return `${label}: $${value.toFixed(2)} (${percentage.toFixed(1)}%)`;
                        }
                    }
                }
            }
        }
    });
    
    // Create bar chart
    const barCtx = document.getElementById('barChart').getContext('2d');
    barChart = new Chart(barCtx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Amount Spent',
                data: amounts,
                backgroundColor: colors,
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `$${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value;
                        }
                    }
                }
            }
        }
    });
}

// Update recommendations
function updateRecommendations(recommendations) {
    const container = document.getElementById('recommendations');
    
    if (!recommendations || recommendations.length === 0) {
        container.innerHTML = '<p style="color: #6b7280;">No recommendations available yet. Add some transactions to get started!</p>';
        return;
    }
    
    container.innerHTML = recommendations.map(rec => `
        <div class="recommendation-card ${rec.priority === 1 ? 'warning' : ''}">
            <div class="recommendation-header">
                <span class="recommendation-priority">Priority ${rec.priority}</span>
                <span class="recommendation-source">${rec.source}</span>
            </div>
            <p class="recommendation-text">${rec.message}</p>
            ${rec.potentialSavings ? 
                `<p class="recommendation-savings">💰 Potential savings: $${rec.potentialSavings.toFixed(2)}/month</p>` 
                : ''}
        </div>
    `).join('');
}

// Update category table
function updateCategoryTable(categories) {
    const tbody = document.getElementById('categoryTableBody');
    
    if (!categories || categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #6b7280;">No data available</td></tr>';
        return;
    }
    
    tbody.innerHTML = categories.map(cat => {
        const diff = cat.percentage - cat.recommendedPercentage;
        const statusClass = diff > 5 ? 'above' : diff < -5 ? 'below' : 'on-track';
        const statusText = diff > 5 ? `+${diff.toFixed(1)}%` : diff < -5 ? `${diff.toFixed(1)}%` : 'On Track';
        
        return `
            <tr>
                <td>
                    <span class="category-badge" style="background: ${CATEGORY_COLORS[cat.category]}20; color: ${CATEGORY_COLORS[cat.category]}">
                        ${CATEGORY_ICONS[cat.category]} ${cat.categoryDisplayName}
                    </span>
                </td>
                <td><strong>$${cat.totalAmount.toFixed(2)}</strong></td>
                <td>${cat.transactionCount}</td>
                <td>${cat.percentage.toFixed(1)}%</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
            </tr>
        `;
    }).join('');
}

// Update recent transactions
function updateRecentTransactions(transactions) {
    const container = document.getElementById('recentTransactions');
    
    if (!transactions || transactions.length === 0) {
        container.innerHTML = '<p style="color: #6b7280;">No recent transactions</p>';
        return;
    }
    
    container.innerHTML = transactions.slice(0, 5).map(txn => `
        <div class="transaction-item">
            <div class="transaction-info">
                <span class="transaction-icon">${CATEGORY_ICONS[txn.category] || '📦'}</span>
                <div class="transaction-details">
                    <h4>${txn.merchantName}</h4>
                    <p>${txn.description || 'No description'} • ${new Date(txn.transactionDate).toLocaleDateString()}</p>
                </div>
            </div>
            <div class="transaction-amount">$${txn.amount.toFixed(2)}</div>
        </div>
    `).join('');
}

// Add new transaction
async function addTransaction(event) {
    event.preventDefault();
    
    const formData = {
        merchantName: document.getElementById('merchantName').value,
        description: document.getElementById('description').value || null,
        amount: parseFloat(document.getElementById('amount').value),
        transactionDate: document.getElementById('transactionDate').value || null
    };
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            throw new Error('Failed to add transaction');
        }
        
        const transaction = await response.json();
        
        showToast(`Transaction added! AI categorized as: ${transaction.category}`, 'success');
        
        // Reset form
        document.getElementById('transactionForm').reset();
        
        // Reload dashboard
        setTimeout(() => loadDashboard(), 500);
        
    } catch (error) {
        console.error('Error adding transaction:', error);
        showToast('Failed to add transaction', 'error');
    } finally {
        showLoading(false);
    }
}

// Show/hide loading overlay
function showLoading(show) {
    document.getElementById('loadingOverlay').style.display = show ? 'flex' : 'none';
}

// Show toast notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.className = 'toast';
    }, 3000);
}

// Format date for display
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric', 
        year: 'numeric' 
    });
}
