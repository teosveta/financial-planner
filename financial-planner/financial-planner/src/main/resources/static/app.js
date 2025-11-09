// API Base URL - Dynamic based on current host
const API_BASE_URL = `${window.location.origin}/api/v1/transactions`;

// Chart instance
let spendingChart = null;

// Category icons mapping
const CATEGORY_ICONS = {
    FOOD: '🍔',
    TRAVEL: '✈️',
    BILLS: '💡',
    ENTERTAINMENT: '🎬',
    SHOPPING: '🛍️',
    HEALTH: '💊',
    TRANSPORT: '🚗',
    OTHER: '📦'
};

// Category colors for chart
const CATEGORY_COLORS = {
    FOOD: '#ef4444',
    TRAVEL: '#06b6d4',
    BILLS: '#f59e0b',
    ENTERTAINMENT: '#a855f7',
    SHOPPING: '#ec4899',
    HEALTH: '#10b981',
    TRANSPORT: '#3b82f6',
    OTHER: '#6b7280'
};

// AI Status tracking
let aiStatus = {
    available: false,
    lastCheck: null
};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    checkAIStatus();
    loadAnalysis();
    loadTransactions();
});

// Tab Navigation
function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    // Remove active from all nav buttons
    document.querySelectorAll('.nav-item').forEach(button => {
        button.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(tabName).classList.add('active');

    // Add active to clicked button
    event.target.closest('.nav-item').classList.add('active');

    // Update page title
    const titles = {
        'dashboard': {
            title: 'Dashboard',
            subtitle: 'Overview of your financial health'
        },
        'transactions': {
            title: 'Transactions',
            subtitle: 'View and manage your transaction history'
        },
        'add-transaction': {
            title: 'Add Transaction',
            subtitle: 'Record a new expense with automatic categorization'
        }
    };

    if (titles[tabName]) {
        document.getElementById('pageTitle').textContent = titles[tabName].title;
        document.getElementById('pageSubtitle').textContent = titles[tabName].subtitle;
    }

    // Load data for specific tabs
    if (tabName === 'dashboard') {
        loadAnalysis();
    } else if (tabName === 'transactions') {
        loadTransactions();
    }
}

// Period selector function
function changePeriod(period) {
    // Update active tab
    document.querySelectorAll('.period-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Update hidden select (for compatibility)
    const periodMap = {
        'weekly': 'weekly',
        'monthly': 'monthly',
        'yearly': 'yearly'
    };
    
    // Store current period for loadAnalysis
    window.currentPeriod = periodMap[period];
    
    loadAnalysis();
}

// Load Analysis Report
async function loadAnalysis() {
    const period = window.currentPeriod || 'monthly';
    const periodLabel = document.getElementById('periodLabel');

    // Update period label
    const periodLabels = {
        'monthly': 'This Month',
        'weekly': 'This Week',
        'yearly': 'This Year'
    };
    periodLabel.textContent = periodLabels[period];

    try {
        const response = await fetch(`${API_BASE_URL}/analysis?period=${period}`);
        if (!response.ok) throw new Error('Failed to load analysis');

        const data = await response.json();
        displayAnalysis(data);
    } catch (error) {
        console.error('Error loading analysis:', error);
        showError('Failed to load analysis data');
    }
}

// Display Analysis Data
function displayAnalysis(data) {
    // Update summary cards
    document.getElementById('totalExpenses').textContent = 
        `$${data.totalExpenses.toFixed(2)}`;

    const totalTransactions = data.categoryBreakdown.reduce(
        (sum, cat) => sum + cat.transactionCount, 0
    );
    document.getElementById('totalTransactions').textContent = totalTransactions;

    // Top category
    if (data.categoryBreakdown.length > 0) {
        const topCat = data.categoryBreakdown[0];
        document.getElementById('topCategory').textContent = 
            topCat.categoryDisplayName;
        document.getElementById('topCategoryAmount').textContent = 
            `$${topCat.totalAmount.toFixed(2)}`;
    }

    // Display chart
    displayChart(data.categoryBreakdown);

    // Display recommendations
    displayRecommendations(data.aiRecommendations);

    // Display category table
    displayCategoryTable(data.categoryBreakdown);
}

// Display Pie Chart
function displayChart(categories) {
    const canvas = document.getElementById('spendingChart');
    const noDataMessage = document.getElementById('noDataMessage');

    if (categories.length === 0) {
        canvas.style.display = 'none';
        noDataMessage.style.display = 'block';
        return;
    }

    canvas.style.display = 'block';
    noDataMessage.style.display = 'none';

    // Destroy existing chart
    if (spendingChart) {
        spendingChart.destroy();
    }

    const ctx = canvas.getContext('2d');

    const labels = categories.map(cat => cat.categoryDisplayName);
    const amounts = categories.map(cat => cat.totalAmount);
    const colors = categories.map(cat => CATEGORY_COLORS[cat.category] || '#6b7280');

    spendingChart = new Chart(ctx, {
        type: 'pie',
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
                            const percentage = categories[context.dataIndex].percentage;
                            return `${label}: $${value.toFixed(2)} (${percentage.toFixed(1)}%)`;
                        }
                    }
                }
            }
        }
    });
}

// Check AI Status
async function checkAIStatus() {
    const indicator = document.getElementById('aiStatusIndicator');
    
    try {
        const response = await fetch(`${API_BASE_URL}/ai/status`);
        if (!response.ok) throw new Error('Failed to check AI status');

        const data = await response.json();
        aiStatus = {
            available: data.available,
            lastCheck: new Date(),
            models: data.models || [],
            message: data.message
        };
        
        // Update indicator
        if (indicator) {
            if (data.available) {
                indicator.textContent = '✓';
                indicator.style.color = '#10b981';
                indicator.style.opacity = '1';
                indicator.title = 'AI Active';
            } else {
                indicator.textContent = '✗';
                indicator.style.color = '#ef4444';
                indicator.style.opacity = '1';
                indicator.title = 'AI Unavailable';
            }
        }
    } catch (error) {
        console.error('Error checking AI status:', error);
        aiStatus = {
            available: false,
            lastCheck: new Date(),
            message: 'Could not check AI status'
        };
        
        // Update indicator to show error
        if (indicator) {
            indicator.textContent = '✗';
            indicator.style.color = '#ef4444';
            indicator.style.opacity = '1';
            indicator.title = 'AI Unavailable';
        }
    }
}

// Update AI Status UI - removed to clean up interface

// Display AI Recommendations
function displayRecommendations(recommendations) {
    const container = document.getElementById('recommendations');

    if (!recommendations || recommendations.length === 0) {
        container.innerHTML = '<div class="empty-state" style="display: flex;"><p>No recommendations available yet.</p></div>';
        return;
    }

    container.innerHTML = recommendations.map(rec => {
        if (!rec || typeof rec !== 'string') {
            return '';
        }
        // Skip any fallback messages about AI configuration
        const normalized = rec.toLowerCase();
        if (normalized.includes('fallback mode') || normalized.includes('api key not configured')) {
            return '';
        }

        // Determine recommendation type for styling
        let className = 'recommendation-item';
        if (rec.includes('above average') || rec.includes('save')) {
            className += ' warning';
        } else if (rec.includes('Great job') || rec.includes('well-balanced')) {
            className += ' success';
        }

        return `<div class="${className}">${rec}</div>`;
    }).filter(item => item !== '').join('');
}

// Display Category Table
function displayCategoryTable(categories) {
    const tbody = document.getElementById('categoryTableBody');

    if (categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="no-data">No data available</td></tr>';
        return;
    }

    tbody.innerHTML = categories.map(cat => `
        <tr>
            <td>
                <span class="category-badge" style="background: ${CATEGORY_COLORS[cat.category]}">
                    ${CATEGORY_ICONS[cat.category]} ${cat.categoryDisplayName}
                </span>
            </td>
            <td><strong>$${cat.totalAmount.toFixed(2)}</strong></td>
            <td>${cat.transactionCount}</td>
            <td>${cat.percentage.toFixed(1)}%</td>
        </tr>
    `).join('');
}

// Add Transaction
async function addTransaction(event) {
    event.preventDefault();

    const formData = {
        merchantName: document.getElementById('merchantName').value,
        description: document.getElementById('description').value,
        amount: parseFloat(document.getElementById('amount').value),
        transactionDate: document.getElementById('transactionDate').value || null
    };

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) throw new Error('Failed to add transaction');

        const transaction = await response.json();

        // Show success message
        showMessage('success', 
            `Transaction added successfully! Categorized as: ${transaction.categoryDisplayName}`);

        // Reset form
        document.getElementById('transactionForm').reset();

        // Reload analysis if on dashboard
        loadAnalysis();
    } catch (error) {
        console.error('Error adding transaction:', error);
        showMessage('error', 'Failed to add transaction. Please try again.');
    }
}

// Load All Transactions
async function loadTransactions() {
    const container = document.getElementById('transactionsList');
    container.innerHTML = `
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Loading transactions...</p>
        </div>
    `;

    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to load transactions');

        const transactions = await response.json();
        displayTransactions(transactions);
    } catch (error) {
        console.error('Error loading transactions:', error);
        container.innerHTML = `
            <div class="empty-state" style="display: flex; color: var(--danger);">
                <p>Failed to load transactions</p>
                <span>Please try refreshing the page</span>
            </div>
        `;
    }
}

// Display Transactions List
function displayTransactions(transactions) {
    const container = document.getElementById('transactionsList');

    if (transactions.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="display: flex;">
                <svg width="64" height="64" viewBox="0 0 20 20" fill="currentColor" opacity="0.3">
                    <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
                    <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/>
                </svg>
                <p>No transactions yet</p>
                <span>Add your first transaction to get started</span>
            </div>
        `;
        return;
    }

    // Sort by date (newest first)
    transactions.sort((a, b) => 
        new Date(b.transactionDate) - new Date(a.transactionDate)
    );

    container.innerHTML = transactions.map(transaction => `
        <div class="transaction-card">
            <div class="transaction-icon" 
                 style="background: ${CATEGORY_COLORS[transaction.category]}20">
                ${CATEGORY_ICONS[transaction.category]}
            </div>
            <div class="transaction-details">
                <h3>${transaction.merchantName}</h3>
                <p>${transaction.description}</p>
                <p style="font-size: 0.85rem; color: var(--gray-500);">
                    ${formatDate(transaction.transactionDate)} • 
                    <span class="category-badge" style="background: ${CATEGORY_COLORS[transaction.category]}; padding: 2px 8px;">
                        ${transaction.categoryDisplayName}
                    </span>
                </p>
            </div>
            <div class="transaction-amount">
                $${transaction.amount.toFixed(2)}
            </div>
            <button class="btn btn-danger btn-sm" onclick="deleteTransaction(${transaction.id})">
                Delete
            </button>
        </div>
    `).join('');
}

// Delete Transaction
async function deleteTransaction(id) {
    if (!confirm('Are you sure you want to delete this transaction?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete transaction');

        // Reload transactions and analysis
        loadTransactions();
        loadAnalysis();

        showMessage('success', 'Transaction deleted successfully!');
    } catch (error) {
        console.error('Error deleting transaction:', error);
        showMessage('error', 'Failed to delete transaction.');
    }
}

// Show Message
function showMessage(type, message) {
    const messageEl = document.getElementById('formMessage');
    messageEl.className = `alert ${type}`;
    messageEl.textContent = message;
    messageEl.style.display = 'block';

    setTimeout(() => {
        messageEl.style.display = 'none';
    }, 5000);
}

// Show Error (for analysis failures)
function showError(message) {
    const container = document.getElementById('recommendations');
    container.innerHTML = `<div class="empty-state" style="display: flex; color: var(--danger);"><p>${message}</p></div>`;
}

// Format Date
function formatDate(dateString) {
    const date = new Date(dateString);
    const options = { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return date.toLocaleDateString('en-US', options);
}

// Update AI Banner - removed for cleaner interface

// Get financial tips from AI
async function getFinancialTips() {
    try {
        const response = await fetch(`${API_BASE_URL}/ai/tips`);
        if (!response.ok) throw new Error('Failed to get tips');
        
        const data = await response.json();
        
        if (data.success) {
            alert('💡 Financial Tips from AI:\n\n' + data.tips);
        } else {
            alert('⚠️ Unable to generate tips at this time');
        }
    } catch (error) {
        console.error('Error getting tips:', error);
        alert('❌ Failed to get AI tips');
    }
}

// Get spending insight from AI
async function getSpendingInsight() {
    const period = window.currentPeriod || 'monthly';
    
    try {
        const response = await fetch(`${API_BASE_URL}/ai/insights?period=${period}`);
        if (!response.ok) throw new Error('Failed to get insight');
        
        const data = await response.json();
        
        if (data.success) {
            alert(`📊 Deep Insight for ${data.period}:\n\n${data.insight}\n\n` +
                  `Total Expenses: $${data.totalExpenses.toFixed(2)}`);
        } else {
            alert('⚠️ Failed to generate insight');
        }
    } catch (error) {
        console.error('Error getting insight:', error);
        alert('❌ Failed to get AI insight');
    }
}
