# 💰 Financial Planner - AI-Powered Budget Tracker

A production-ready personal finance management application built with **Spring Boot 3.5** and vanilla JavaScript. Features automatic transaction categorization and AI-powered savings recommendations.

## 🚀 Features

### Backend (Spring Boot)
- **📥 JSON Import**: Batch import transactions from JSON files with automatic analysis
- **Automatic Transaction Categorization**: Rule-based system categorizes expenses into 8 categories
- **🤖 REAL AI Integration**: Powered by Ollama with Llama 3.2, Mistral, Phi-3, or other models
- **AI Recommendation Engine**: Generates truly personalized savings advice using local LLMs
- **Statistical Analysis**: Weekly, monthly, and yearly expense breakdowns
- **RESTful API**: Clean, documented endpoints following REST best practices
- **Production-Ready**: Exception handling, validation, logging, and database transactions
- **Webhook Support**: Integration with Digital Wallet microservice

### AI Capabilities
- **Local AI Models**: Run Llama 3.2, Mistral 7B, Phi-3, or other models via Ollama
- **Intelligent Prompts**: Structured financial data sent to AI for analysis
- **Real-time Insights**: AI analyzes spending patterns and provides actionable advice
- **Privacy-First**: All AI processing happens locally on your machine
- **Fallback Mechanism**: Graceful degradation when AI is unavailable
- **Multiple Models**: Easily switch between different AI models

### Frontend (HTML/CSS/JavaScript)
- **Interactive Dashboard**: Real-time spending visualization with Chart.js
- **Transaction Management**: Add, view, and delete transactions
- **AI Insights**: Visual display of personalized recommendations
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Modern UI**: Clean, professional interface with smooth animations

### Transaction Categories
1. 🍔 **Food & Dining** - Restaurants, groceries, coffee shops
2. ✈️ **Travel** - Hotels, flights, vacation bookings
3. 💡 **Bills & Utilities** - Electric, internet, phone services
4. 🎬 **Entertainment** - Streaming services, movies, games
5. 🛍️ **Shopping** - Retail stores, online purchases
6. 💊 **Health & Wellness** - Gyms, pharmacies, medical services
7. 🚗 **Transportation** - Uber, taxis, gas stations
8. 📦 **Other** - Miscellaneous expenses

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- Modern web browser (Chrome, Firefox, Safari, Edge)
- **Ollama** (Optional - for AI features)
  - Download: https://ollama.com
  - See [AI_INTEGRATION_GUIDE.md](AI_INTEGRATION_GUIDE.md) for setup

## 🤖 AI Setup (5 Minutes - Required for Real AI)

### ⚡ Quick Start

**Windows:**
```cmd
cd financial-planner
setup-ollama.bat
```

**Mac/Linux:**
```bash
cd financial-planner
chmod +x setup-ollama.sh
./setup-ollama.sh
```

The setup script will:
- ✅ Check if Ollama is installed
- ✅ Start Ollama service
- ✅ Download AI model (llama3.2)
- ✅ Test AI integration
- ✅ Verify everything works

### 📖 Detailed Guides

- **Quick Start (5 min)**: [QUICK_AI_START.md](QUICK_AI_START.md) ⚡
- **Full Setup Guide**: [AI_SETUP_GUIDE.md](AI_SETUP_GUIDE.md) 📚

### 🔍 How to Know if AI is Working

Open the app at http://localhost:8080 and look at the **"AI Insights"** section:

- **✅ Real AI Active** → AI is generating personalized recommendations!
- **⚠️ Fallback Mode** → Using rule-based recommendations (run setup script)

**Without Ollama**: App works with intelligent fallback recommendations, but you'll miss the real AI magic! 🪄

## 🛠️ Installation & Setup

### 1. Clone or Download the Project
```bash
cd financial-planner
```

### 2. Build the Project
```bash
./mvnw clean install
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

Or on Windows:
```cmd
mvnw.cmd spring-boot:run
```

### 4. Access the Application
Open your browser and navigate to:
```
http://localhost:8080
```

The application will automatically:
- Initialize the H2 in-memory database
- Create sample transactions for testing
- Start the web server on port 8080

## 📥 JSON Import (New!)

Import multiple transactions at once from JSON files:

```bash
# Quick test with example file
curl -X POST http://localhost:8080/api/v1/import/file \
  -F "file=@example-import.json"
```

**Response includes:**
- ✅ Import summary (success/fail counts)
- ✅ Automatic categorization
- ✅ AI analysis and recommendations
- ✅ Duplicate detection

📖 **Full Guide**: [JSON_IMPORT_GUIDE.md](JSON_IMPORT_GUIDE.md)

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1/transactions
http://localhost:8080/api/v1/import
```

### Transaction Endpoints

#### 1. Create Transaction
```http
POST /api/v1/transactions
Content-Type: application/json

{
  "merchantName": "Starbucks",
  "description": "Morning coffee",
  "amount": 5.99,
  "transactionDate": "2024-11-08T09:30:00"
}
```

**Response**: Transaction object with auto-assigned category

#### 2. Get All Transactions
```http
GET /api/v1/transactions
```

#### 3. Get Transaction by ID
```http
GET /api/v1/transactions/{id}
```

#### 4. Get Transactions by Category
```http
GET /api/v1/transactions/category/FOOD
```

#### 5. Get Analysis Report
```http
GET /api/v1/transactions/analysis?period=monthly
```

**Query Parameters**:
- `period`: `weekly`, `monthly`, or `yearly`

**Response**:
```json
{
  "period": "monthly",
  "totalExpenses": 1234.56,
  "categoryBreakdown": [...],
  "aiRecommendations": [
    "Your food expenses are 40% of your budget - 10% above average..."
  ]
}
```

#### 6. Get Category Statistics
```http
GET /api/v1/transactions/stats/FOOD?period=monthly
```

#### 7. Delete Transaction
```http
DELETE /api/v1/transactions/{id}
```

#### 8. Health Check
```http
GET /api/v1/transactions/health
```

### Import Endpoints

#### 1. Import from File
```http
POST /api/v1/import/file
Content-Type: multipart/form-data

file: transactions.json
```

#### 2. Import from JSON Data
```http
POST /api/v1/import/data
Content-Type: application/json

{
  "user": {
    "userId": "user123",
    "username": "john_doe"
  },
  "transactions": [
    {
      "description": "Coffee",
      "merchantName": "Starbucks",
      "amount": 5.99,
      "transactionDate": "2024-11-08T09:00:00"
    }
  ]
}
```

#### 3. Get Import Example
```http
GET /api/v1/import/example
```

See [JSON_IMPORT_GUIDE.md](JSON_IMPORT_GUIDE.md) for complete documentation.

## 🏗️ Architecture

### Project Structure
```
financial-planner/
├── src/
│   ├── main/
│   │   ├── java/com/financialplanner/
│   │   │   ├── FinancialPlannerApplication.java
│   │   │   ├── controller/
│   │   │   │   └── TransactionController.java
│   │   │   ├── service/
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── TransactionAnalysisService.java
│   │   │   │   ├── TransactionCategorizationService.java
│   │   │   │   └── AIRecommendationEngine.java
│   │   │   ├── repository/
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── model/
│   │   │   │   └── Transaction.java
│   │   │   ├── dto/
│   │   │   │   └── TransactionDTO.java
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── styles.css
│   │           └── app.js
│   └── test/
│       └── java/com/financialplanner/
└── pom.xml
```

### Design Patterns Used

1. **Repository Pattern** - Data access abstraction
2. **Service Layer Pattern** - Business logic separation
3. **DTO Pattern** - API request/response objects
4. **Strategy Pattern** - Rule-based categorization
5. **Builder Pattern** - Clean object construction
6. **Singleton Pattern** - Service beans

### Key Spring Boot Best Practices

✅ **Separation of Concerns** - Clear controller/service/repository layers
✅ **Dependency Injection** - Constructor injection with `@RequiredArgsConstructor`
✅ **Validation** - `@Valid` annotations with Bean Validation
✅ **Exception Handling** - Global `@RestControllerAdvice`
✅ **Transactional Management** - `@Transactional` for data consistency
✅ **Logging** - SLF4J with Lombok's `@Slf4j`
✅ **Configuration Management** - Externalized in `application.properties`
✅ **RESTful API Design** - Proper HTTP methods and status codes

## 🧠 AI Recommendation Logic

The recommendation engine analyzes spending patterns and compares them to industry averages:

- **Food**: Average 30%, alerts if >35%
- **Transport**: Average 15%, alerts if >20%
- **Bills**: Average 25%, alerts if >30%
- **Entertainment**: Average 10%, alerts if >15%
- **Shopping**: Average 15%, alerts if >20%
- **Health**: Average 5%, alerts if <3% or >10%

Recommendations include:
- Percentage comparisons to averages
- Specific savings strategies per category
- Calculated potential monthly savings (20% reduction target)
- General financial wellness tips

## 🎨 Frontend Features

### Dashboard
- Real-time pie chart visualization
- Summary cards with total expenses, transaction count, top category
- Period selector (weekly/monthly/yearly)
- Category breakdown table

### Add Transaction
- Instant form validation
- Auto-categorization preview
- Success/error messaging
- Form reset after submission

### Transaction History
- Chronological list with newest first
- Category badges with icons
- Delete functionality with confirmation
- Formatted dates and amounts

## 🔧 Customization

### Adding New Categories

1. Edit `Transaction.java`:
```java
public enum TransactionCategory {
    // Add your category
    CUSTOM("Custom Category"),
    // ...
}
```

2. Update `TransactionCategorizationService.java`:
```java
rules.put(compilePattern("your|keywords|here"), 
          TransactionCategory.CUSTOM);
```

3. Add to frontend `app.js`:
```javascript
const CATEGORY_ICONS = {
    CUSTOM: '🎯',
    // ...
};

const CATEGORY_COLORS = {
    CUSTOM: '#ff6b6b',
    // ...
};
```

### Modifying AI Thresholds

Edit `AIRecommendationEngine.java`:
```java
private static final Map<TransactionCategory, Double> AVERAGE_PERCENTAGES = Map.of(
    TransactionCategory.FOOD, 30.0,  // Adjust percentages
    // ...
);

private static final double THRESHOLD_PERCENTAGE = 5.0;  // Adjust sensitivity
```

### Switching to Production Database

Replace H2 with MySQL/PostgreSQL in `pom.xml` and `application.properties`:

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/financial_planner
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Testing

### Access H2 Console (Development)
```
http://localhost:8080/h2-console
```

**Connection Details**:
- JDBC URL: `jdbc:h2:mem:financialdb`
- Username: `sa`
- Password: (leave empty)

### Sample Data
The application includes 15 pre-loaded transactions across all categories for testing.

## 📈 Future Enhancements

Potential features for extension:

- [ ] User authentication and multi-user support
- [ ] Budget setting and alerts
- [ ] Recurring transaction detection
- [ ] Export to CSV/PDF
- [ ] Integration with bank APIs
- [ ] Machine learning categorization
- [ ] Mobile app (React Native)
- [ ] Email notifications
- [ ] Goal tracking and savings plans
- [ ] Investment portfolio tracking

## 🤝 Contributing

This is a learning project demonstrating Spring Boot best practices. Feel free to fork and extend!

## 📝 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

Built with ☕ and 💙 using Spring Boot 3.5 and modern web technologies.

---

**Happy budgeting! 💰📊**
