# 💰 Hack-Cash Financial Planner Microservice

**AI-Powered Financial Planning & Spending Analysis**

A production-ready Spring Boot microservice that provides intelligent transaction categorization, spending analytics, and personalized AI recommendations for the Hack-Cash Digital Wallet ecosystem.

---

## 🎯 Key Features

### 🤖 **Real AI Integration (Not Hardcoded!)**
- **OpenAI GPT-4** for transaction categorization
- **AI-powered savings recommendations** with actionable insights
- **Fallback logic** ensures reliability even if API fails

### 📊 **Intelligent Analytics**
- Automatic transaction categorization into 11 categories
- Spending trend analysis (daily, weekly, monthly, yearly)
- Category benchmarking against financial best practices
- Savings rate calculation and recommendations

### 🎨 **Modern Dashboard**
- Beautiful responsive UI with Chart.js visualizations
- Real-time pie charts and bar graphs
- Category breakdown tables with health indicators
- Recent transaction timeline

### 🔗 **Microservices Architecture**
- Runs independently on **port 8081**
- Integrates with main Hack-Cash wallet (port 8080) via JSON import
- REST API for seamless communication
- Health monitoring endpoints for Kubernetes deployment

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Maven 3.8+**
- **OpenAI API Key** (already configured in application.properties)

### Installation

```bash
# Navigate to project directory
cd financial-planner-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The service will start on **http://localhost:8081**

### Access the Dashboard
Open your browser and navigate to:
```
http://localhost:8081/index.html
```

---

## 📁 Project Structure

```
financial-planner-service/
├── src/main/java/com/hackcash/financialplanner/
│   ├── FinancialPlannerApplication.java       # Main Spring Boot app
│   ├── controller/
│   │   └── FinancialPlannerController.java    # REST API endpoints
│   ├── service/
│   │   ├── OpenAIService.java                 # Real OpenAI integration
│   │   ├── TransactionService.java            # CRUD + JSON import
│   │   ├── TransactionCategorizationService.java  # AI + rule-based categorization
│   │   ├── TransactionAnalysisService.java    # Spending analytics
│   │   └── AIRecommendationEngine.java        # AI recommendations with fallback
│   ├── model/
│   │   ├── Transaction.java                   # JPA entity
│   │   └── TransactionCategory.java           # Enum with 11 categories
│   ├── repository/
│   │   └── TransactionRepository.java         # JPA queries
│   ├── dto/
│   │   ├── TransactionDTO.java
│   │   ├── SpendingAnalysisDTO.java
│   │   ├── CategoryAnalysisDTO.java
│   │   └── RecommendationDTO.java
│   ├── config/
│   │   └── WebClientConfig.java               # HTTP client config
│   └── exception/
│       └── GlobalExceptionHandler.java         # Error handling
├── src/main/resources/
│   ├── application.properties                  # Config with OpenAI API key
│   ├── wallet-transactions.json                # Sample data for import
│   └── static/
│       ├── index.html                          # Dashboard UI
│       ├── styles.css                          # Modern styling
│       └── app.js                              # Frontend logic with Chart.js
└── pom.xml                                     # Maven dependencies
```

---

## 🔌 API Endpoints

### **Transaction Management**
```
POST   /api/v1/transactions              # Create transaction (auto-categorized)
GET    /api/v1/transactions              # Get all transactions
GET    /api/v1/transactions/recent       # Get recent transactions
GET    /api/v1/transactions/{id}         # Get transaction by ID
DELETE /api/v1/transactions/{id}         # Delete transaction
```

### **Analytics & Insights**
```
GET    /api/v1/analysis                  # Spending analysis for period
       ?period=1&unit=month              # Parameters: period (number), unit (day/week/month/year)

GET    /api/v1/recommendations           # AI-powered recommendations
       ?period=1&unit=month

GET    /api/v1/dashboard                 # Complete dashboard data
       ?period=1&unit=month              # (analysis + recommendations + recent transactions)

GET    /api/v1/trends                    # Spending trend over months
       ?months=6
```

### **System Health**
```
GET    /api/v1/health                    # Health check (Kubernetes-ready)
POST   /api/v1/import/trigger            # Manual JSON import trigger
```

---

## 🧠 AI Categorization Logic

### **Primary: OpenAI GPT-4**
1. Sends merchant name, description, and amount to OpenAI
2. GPT-4 analyzes context and returns category
3. 85% confidence score for AI categorizations

### **Fallback: Rule-Based**
If OpenAI fails (API down, rate limit, etc.):
1. Regex pattern matching on merchant/description
2. 65% confidence score for rule-based categorizations
3. Ensures service never fails completely

### **Supported Categories**
```
🍔 FOOD             - Restaurants, cafes, fast food
✈️ TRAVEL           - Airlines, hotels, travel bookings
📄 BILLS            - Utilities, rent, insurance, subscriptions
🎬 ENTERTAINMENT    - Movies, streaming, games, events
🛍️ SHOPPING         - Retail, clothing, electronics
💊 HEALTH           - Pharmacy, doctor, gym, fitness
🚗 TRANSPORT        - Uber, gas, parking, public transit
📚 EDUCATION        - Courses, books, tuition
🛒 GROCERIES        - Supermarkets, produce
💰 INCOME           - Salary, bonuses, refunds
💎 SAVINGS          - Investments, savings transfers
📦 OTHER            - Uncategorized
```

---

## 💡 AI Recommendations Engine

### **Primary: OpenAI Analysis**
- Sends complete spending data to GPT-4
- Receives personalized, context-aware recommendations
- 3-5 actionable suggestions per analysis

### **Fallback: Rule-Based Intelligence**
If OpenAI unavailable:
1. **Overspending Detection**: Flags categories >10% above benchmark
2. **Savings Rate Analysis**: Recommends 20% savings if below target
3. **Trend Analysis**: Warns if spending increased >10% vs previous period
4. **Category-Specific Tips**: Provides practical advice based on category

**Example Recommendations:**
```
✅ "Your food expenses are 40% of your budget - 10% above recommended. 
    You could save $150/month by reducing food spending by 20%."

✅ "Your savings rate is 8%. Financial experts recommend saving at least 
    20% of your income. Try the 50/30/20 rule."

✅ "Try meal planning and cooking at home 4-5 days a week to reduce 
    food expenses significantly."
```

---

## 📥 JSON Import from Main Wallet

The service automatically imports transactions from the main Hack-Cash wallet on startup.

### **Configure Import Path**
Edit `application.properties`:
```properties
wallet.data.import.path=classpath:wallet-transactions.json
wallet.data.import.enabled=true
```

### **Supported JSON Format**
```json
{
  "transactions": [
    {
      "merchantName": "Starbucks Coffee",
      "description": "Morning coffee",
      "amount": 12.50,
      "transactionDate": "2025-01-15T08:30:00",
      "category": "FOOD",  // Optional - will auto-categorize if missing
      "walletId": "wallet-123",
      "userId": "user-456"
    }
  ]
}
```

The import is **flexible** and supports various field names:
- `merchantName` / `merchant` / `sender` / `receiver`
- `description` / `details` / `memo`
- `amount` / `value`
- `transactionDate` / `date` / `timestamp`

---

## 🔧 Configuration

### **OpenAI API Settings**
```properties
openai.api.key=YOUR_API_KEY_HERE
openai.api.url=https://api.openai.com/v1/chat/completions
openai.model=gpt-4
openai.timeout=30000
```

### **Database Configuration**
Currently uses **H2 in-memory** database for demo purposes.

For **production**, update to PostgreSQL:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financial_planner
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### **Caching**
Enabled for performance optimization:
```properties
spring.cache.type=simple
spring.cache.cache-names=ai-recommendations,category-analysis
```

---

## 🎨 Dashboard Features

### **Summary Cards**
- Total Expenses
- Total Income
- Savings Rate (with color coding)
- Spending Trend (vs previous period)

### **Interactive Charts**
- **Pie Chart**: Spending distribution by category
- **Bar Chart**: Category comparison

### **AI Recommendations Panel**
- Priority-ranked suggestions
- Source indicator (AI vs Rule-Based)
- Potential savings calculations

### **Category Analysis Table**
- Spending vs recommended benchmarks
- Transaction counts
- Health status indicators

### **Recent Transactions**
- Last 10 transactions with icons
- Category badges
- Date and amount display

---

## 🚀 Deployment

### **Docker**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/financial-planner-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### **Kubernetes**
The service includes health endpoints for Kubernetes:
```yaml
livenessProbe:
  httpGet:
    path: /api/v1/health
    port: 8081
readinessProbe:
  httpGet:
    path: /api/v1/health
    port: 8081
```

---

## 🎯 Hackathon Demo Tips

### **Highlight These Points to Judges:**

1. **🤖 Real AI Integration**
   - "We're using actual OpenAI GPT-4 API, not hardcoded responses"
   - Show live categorization of a new transaction
   - Demonstrate fallback logic by temporarily disabling API

2. **🏗️ Production-Ready Architecture**
   - "Microservices design with proper separation of concerns"
   - "REST APIs for seamless integration"
   - "Kubernetes-ready with health monitoring"

3. **📊 Intelligent Analytics**
   - Show category benchmarking: "Food is 40% vs recommended 30%"
   - Demonstrate AI recommendations with potential savings
   - Highlight spending trends and comparisons

4. **🎨 Modern UX**
   - "Responsive dashboard with Chart.js visualizations"
   - "Real-time updates after adding transactions"
   - "Color-coded health indicators"

5. **🔗 Integration Capabilities**
   - "JSON import from main wallet service"
   - "Flexible data format support"
   - "Event-driven communication ready"

---

## 📝 API Examples

### **Add Transaction (Auto-Categorized)**
```bash
curl -X POST http://localhost:8081/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "merchantName": "Whole Foods",
    "description": "Weekly groceries",
    "amount": 142.50
  }'
```

### **Get Spending Analysis**
```bash
curl http://localhost:8081/api/v1/analysis?period=1&unit=month
```

### **Get AI Recommendations**
```bash
curl http://localhost:8081/api/v1/recommendations?period=1&unit=month
```

### **Get Complete Dashboard**
```bash
curl http://localhost:8081/api/v1/dashboard?period=1&unit=month
```

---

## 🐛 Troubleshooting

### **OpenAI API Not Working**
- Verify API key in `application.properties`
- Check internet connection
- Service will automatically use fallback logic

### **No Transactions Showing**
- Check if JSON import file exists: `src/main/resources/wallet-transactions.json`
- Trigger manual import: `POST /api/v1/import/trigger`
- Add transactions manually via UI or API

### **Charts Not Rendering**
- Clear browser cache
- Check browser console for JavaScript errors
- Ensure Chart.js CDN is accessible

---

## 🔐 Security Notes

⚠️ **For Production:**
1. Move API keys to environment variables
2. Implement authentication/authorization
3. Add rate limiting
4. Enable HTTPS
5. Secure H2 console or disable it

---

## 📚 Tech Stack

- **Backend**: Spring Boot 3.2, Java 17
- **Database**: H2 (demo), PostgreSQL (production-ready)
- **AI**: OpenAI GPT-4 API
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Visualization**: Chart.js
- **HTTP Client**: Spring WebFlux (WebClient)
- **Caching**: Spring Cache
- **Build Tool**: Maven

---

## 🎉 Success Criteria for Hackathon

✅ **Real AI** - Not fake responses  
✅ **Production-grade** - Proper architecture  
✅ **Beautiful UI** - Modern, responsive dashboard  
✅ **Smart Analytics** - Actionable insights  
✅ **Integration-ready** - Works with main wallet  
✅ **Demo-ready** - Sample data pre-loaded  

---

## 📞 Support

For questions or issues during the hackathon:
1. Check application logs: `tail -f logs/application.log`
2. Test health endpoint: `curl http://localhost:8081/api/v1/health`
3. Review H2 console: `http://localhost:8081/h2-console`

---

**Built with ❤️ for Hack-Cash Hackathon**

Good luck with your presentation! 🚀
