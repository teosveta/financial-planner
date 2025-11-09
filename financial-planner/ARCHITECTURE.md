# 📐 Architecture Overview - Claude AI-Powered Financial Planner

## 🗂️ Project Structure

```
financial-planner/
│
├── 📄 pom.xml                          # Maven dependencies
├── 📄 README.md                        # Full documentation
├── 📄 QUICKSTART.md                    # Quick start guide
├── 🚀 start.sh                         # Unix/Mac startup script
├── 🚀 start.bat                        # Windows startup script
│
├── src/main/
│   ├── java/com/financialplanner/
│   │   ├── 📱 FinancialPlannerApplication.java    # Main Spring Boot app
│   │   │
│   │   ├── controller/                            # REST API Layer
│   │   │   └── 🎮 TransactionController.java     # API endpoints
│   │   │
│   │   ├── service/                               # Business Logic Layer
│   │   │   ├── 💼 TransactionService.java         # CRUD operations
│   │   │   ├── 📊 TransactionAnalysisService.java # Statistics & reports
│   │   │   ├── 🏷️  TransactionCategorizationService.java # Smart categorization
│   │   │   ├── 🤖 AIRecommendationEngine.java     # AI recommendation engine
│   │   │   ├── ⚡ ClaudeAIService.java            # Claude AI API (Primary)
│   │   │   ├── 🔄 OllamaAIService.java            # Ollama support (Deprecated)
│   │   │   └── 📥 JsonImportService.java          # Batch import
│   │   │
│   │   ├── repository/                            # Data Access Layer
│   │   │   └── 💾 TransactionRepository.java      # Database queries
│   │   │
│   │   ├── model/                                 # Domain Entities
│   │   │   └── 📦 Transaction.java                # Transaction entity
│   │   │
│   │   ├── dto/                                   # Data Transfer Objects
│   │   │   └── 📋 TransactionDTO.java             # API request/response
│   │   │
│   │   ├── config/                                # Configuration
│   │   │   └── ⚙️  DataInitializer.java           # Sample data loader
│   │   │
│   │   └── exception/                             # Error Handling
│   │       └── ⚠️  GlobalExceptionHandler.java    # Centralized exceptions
│   │
│   └── resources/
│       ├── application.properties                 # App configuration
│       └── static/                                # Frontend files
│           ├── 🌐 index.html                      # Main UI
│           ├── 🎨 styles.css                      # Styling
│           └── ⚡ app.js                          # JavaScript logic
│
└── src/test/
    └── java/com/financialplanner/
        └── 🧪 FinancialPlannerApplicationTests.java # Tests

```

## 🏗️ Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                     FRONTEND LAYER                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐ │
│  │ index.html│  │styles.css│  │      app.js          │ │
│  │  (UI)    │  │ (Design) │  │ (API Integration)    │ │
│  └──────────┘  └──────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                         ↕ HTTP REST API
┌─────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                       │
│  ┌─────────────────────────────────────────────────┐   │
│  │         TransactionController                    │   │
│  │  • POST /api/v1/transactions                    │   │
│  │  • GET  /api/v1/transactions                    │   │
│  │  • GET  /api/v1/transactions/analysis           │   │
│  │  • DELETE /api/v1/transactions/{id}             │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                         │
│  ┌──────────────────┐  ┌──────────────────────────┐   │
│  │TransactionService│  │TransactionAnalysisService│   │
│  │  • Create        │  │  • Calculate stats       │   │
│  │  • Read          │  │  • Generate reports      │   │
│  │  • Delete        │  └──────────────────────────┘   │
│  └──────────────────┘                                   │
│  ┌────────────────────────┐  ┌──────────────────────┐ │
│  │CategorizationService   │  │AIRecommendationEngine│ │
│  │  • Rule-based matching │  │  • Analyze patterns  │ │
│  │  • Auto-categorize     │  │  • Generate advice   │ │
│  └────────────────────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │         TransactionRepository                    │   │
│  │  • findAll()                                    │   │
│  │  • findByCategory()                             │   │
│  │  • findTransactionsInPeriod()                   │   │
│  │  • sumAmountByCategoryInPeriod()                │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │              H2 In-Memory Database              │   │
│  │         (Production: MySQL/PostgreSQL)          │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 🔄 Request Flow Example

### Adding a Transaction

```
1. User fills form → clicks "Add Transaction"
   ↓
2. Frontend (app.js) → POST /api/v1/transactions
   {
     "merchantName": "Starbucks",
     "description": "Morning coffee",
     "amount": 5.99
   }
   ↓
3. TransactionController receives request
   • Validates input (@Valid)
   • Delegates to TransactionService
   ↓
4. TransactionService
   • Calls CategorizationService to auto-categorize
   • Creates Transaction entity
   • Saves via TransactionRepository
   ↓
5. TransactionRepository
   • Persists to database
   • Returns saved entity
   ↓
6. Response flows back up
   {
     "id": 1,
     "merchantName": "Starbucks",
     "category": "FOOD",
     "categoryDisplayName": "Food & Dining",
     ...
   }
   ↓
7. Frontend updates UI
   • Shows success message
   • Refreshes dashboard
   • Updates chart
```

## 🤖 Claude AI Integration Flow

```
1. User adds transaction → "Starbucks Coffee"
   ↓
2. TransactionService.createTransaction()
   ↓
3. TransactionCategorizationService.categorize()
   ├─→ Try: ClaudeAIService.categorizeTransaction()
   │    • Send to Claude API with context
   │    • Receive: "FOOD"
   │    • ✅ Return AI-categorized result
   │
   └─→ Fallback: Rule-based categorization
        • If Claude unavailable
        • Pattern matching rules
        • Return best-guess category
   ↓
4. Transaction saved with category
```

## 🧠 Claude AI Recommendation Flow

```
1. User views Dashboard → period="monthly"
   ↓
2. GET /api/v1/transactions/analysis?period=monthly
   ↓
3. TransactionAnalysisService
   • Queries all transactions in period
   • Calculates category totals & percentages
   • Builds spending profile
   ↓
4. AIRecommendationEngine
   ├─→ Check: ClaudeAIService.isAvailable()
   │
   ├─→ If Available:
   │    • Build intelligent prompt with:
   │      - Spending summary
   │      - Category breakdown
   │      - Industry benchmarks
   │      - Behavioral psychology context
   │    • Call Claude API
   │    • Parse AI response into recommendations
   │    • ✅ Return personalized insights
   │
   └─→ If Unavailable:
        • Use rule-based analysis
        • Compare to averages
        • Calculate potential savings
        • Return fallback recommendations
   ↓
5. Returns comprehensive analysis
   {
     "totalExpenses": 1234.56,
     "categoryBreakdown": [...],
     "aiRecommendations": [
       "🍔 Food at $520 (38%) is high. Meal prep Sundays could save $120/mo.",
       "💡 Bills at $180 (12%) excellent! Consider smart plugs for $15 more savings.",
       "🎯 Entertainment $85 (6%) well-managed - sustainable long-term!"
     ]
   }
   ↓
6. Frontend displays with Claude AI branding
   • Prominent AI status banner
   • Pie chart visualization
   • AI-powered recommendation cards
   • Quick AI action buttons
   • Category breakdown table
```

## 🆕 New AI Endpoints

```
GET  /api/v1/transactions/ai/status
     → Check if Claude AI is available

POST /api/v1/transactions/ai/test
     → Test Claude AI connection

GET  /api/v1/transactions/ai/insights?period=monthly
     → Get deep spending insights from Claude

POST /api/v1/transactions/ai/predict-category
     → Predict category before saving transaction

GET  /api/v1/transactions/ai/tips
     → Get general financial tips from Claude
```

## 🎯 Key Design Patterns

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Repository** | `TransactionRepository` | Data access abstraction |
| **Service Layer** | `service/` package | Business logic encapsulation |
| **DTO** | `TransactionDTO` | Request/response decoupling |
| **Strategy** | `CategorizationService` | Flexible categorization rules |
| **Builder** | All entities/DTOs | Clean object construction |
| **Dependency Injection** | All services | Loose coupling |
| **MVC** | Controller/Service/Repository | Separation of concerns |

## 🔐 Spring Boot Best Practices Applied

✅ **Constructor Injection** - Using `@RequiredArgsConstructor`
✅ **Immutable DTOs** - Using Lombok's `@Data` and `@Builder`
✅ **Validation** - Using `@Valid` and Bean Validation
✅ **Exception Handling** - Global `@RestControllerAdvice`
✅ **Transactional** - `@Transactional` for data consistency
✅ **Logging** - SLF4J with structured logging
✅ **Configuration** - Externalized properties
✅ **RESTful Design** - Proper HTTP methods and status codes
✅ **API Versioning** - `/api/v1/` prefix
✅ **CORS** - Configured for development

## 📊 Database Schema

```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_category ON transactions(category);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
```

## 🚀 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **AI Engine** | **Claude by Anthropic** | **3.5 Sonnet** |
| **Backend Framework** | Spring Boot | 3.5.7 |
| **Language** | Java | 21 |
| **Build Tool** | Maven | 3.9+ |
| **Database (Dev)** | H2 | In-memory |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Validation** | Bean Validation | 3.0 |
| **API Client** | RestTemplate | Spring Web |
| **Frontend** | Vanilla JavaScript | ES6+ |
| **Charts** | Chart.js | 4.x |
| **Styling** | CSS3 | Modern |

## 🎓 Learning Resources

- **Spring Boot**: Official docs at spring.io/projects/spring-boot
- **JPA**: Spring Data JPA reference
- **REST API**: RESTful API design best practices
- **Clean Code**: Robert C. Martin's principles
- **Design Patterns**: Gang of Four patterns

---

**This architecture is production-ready and follows industry best practices!** 🚀
