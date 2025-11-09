# 🏛️ Architecture Overview – Financial Planner

The application is intentionally lean: one Spring Boot service delivers REST endpoints, AI assistance, and static front-end assets. This section documents the moving parts after the cleanup.

## 📂 Project Layout
```
financial-planner/
├── pom.xml
├── README.md
├── ARCHITECTURE.md
├── start.sh / start.bat
└── src
    ├── main
    │   ├── java/com/financialplanner
    │   │   ├── FinancialPlannerApplication.java   # Boot entry point
    │   │   ├── controller/
    │   │   │   └── TransactionController.java     # REST API
    │   │   ├── dto/
    │   │   │   └── TransactionDTO.java            # Request/response models
    │   │   ├── model/
    │   │   │   └── Transaction.java               # JPA entity + enum categories
    │   │   ├── repository/
    │   │   │   └── TransactionRepository.java     # Spring Data queries & aggregates
    │   │   ├── service/
    │   │   │   ├── TransactionService.java
    │   │   │   ├── TransactionAnalysisService.java
    │   │   │   ├── TransactionCategorizationService.java
    │   │   │   ├── AIRecommendationEngine.java
    │   │   │   └── OpenAIService.java
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java           # Sample seed data
    │   │   │   └── WebClientConfig.java           # Shared WebClient builder
    │   │   └── exception/
    │   │       └── GlobalExceptionHandler.java
    │   └── resources
    │       ├── application.properties
    │       └── static/                           # Front-end bundle
    │           ├── index.html
    │           ├── styles.css
    │           └── app.js
    └── test/java/com/financialplanner
        └── FinancialPlannerApplicationTests.java
```

## 🧱 Layered Responsibilities
```
┌─────────────────────────────┐
│         Frontend            │
│  (index.html, styles.css,   │
│   app.js, Chart.js widgets) │
└──────────────┬──────────────┘
               │ REST (JSON)
┌──────────────┴──────────────┐
│       Controller Layer       │
│ TransactionController        │
│  • CRUD endpoints            │
│  • Analysis + AI endpoints   │
└──────────────┬──────────────┘
               │
┌──────────────┴──────────────┐
│         Service Layer        │
│ TransactionService           │
│ TransactionCategorizationSvc │
│ TransactionAnalysisService   │
│ AIRecommendationEngine       │
│ OpenAIService                │
└──────────────┬──────────────┘
               │
┌──────────────┴──────────────┐
│        Repository Layer      │
│ TransactionRepository        │
│  • JPA CRUD                  │
│  • Aggregation queries       │
└──────────────┬──────────────┘
               │
┌──────────────┴──────────────┐
│        Database Layer        │
│   H2 in-memory (dev)         │
│   Swap for MySQL/Postgres    │
└─────────────────────────────┘
```

## 🔄 Key Flows

### 1. Transaction capture
1. UI posts to `POST /api/v1/transactions`.
2. `TransactionService` validates + builds entity.
3. `TransactionCategorizationService`:
   - tries OpenAI classification (`OpenAIService.categorizeTransaction`),
   - falls back to regex rules when AI unavailable/unknown.
4. Record persisted via `TransactionRepository`.
5. DTO response returned to UI to refresh dashboard.

### 2. Period analysis & recommendations
1. UI requests `GET /analysis?period=monthly`.
2. `TransactionAnalysisService`:
   - loads current-period transactions,
   - rolls up totals and percentages by category,
   - fetches previous-period totals for trend comparison,
   - calculates percentage deltas and attaches to DTO.
3. `AIRecommendationEngine`:
   - prepares benchmark/trend prompt for OpenAI,
   - parses AI response into short bullet recommendations,
   - or uses deterministic fallback if API unavailable.
4. Response includes totals, breakdown list, and AI messages.

### 3. AI insight utilities
The controller exposes lightweight helpers:
- `/ai/status` → boolean readiness check.
- `/ai/test`   → manual prompt to verify connectivity.
- `/ai/insights` → summarised insight for the active period.
- `/ai/tips` → general-purpose money tips (OpenAI or fallback message).
- `/ai/predict-category` → quick categorisation preview before form submission.

## 🧠 Recommendation Engine Internals
- Benchmarks per category (Food 30%, Bills 25%, etc.).
- Historical trends derived from previous period totals via repository aggregates.
- Fallback logic triggers when:
  - no API key,
  - HTTP error/timeouts,
  - empty/invalid AI response.
- Savings suggestions target a 20% reduction in categories exceeding benchmarks by ≥5% or rising >10% period-over-period.

## ✅ Best Practices Applied
- Constructor injection through Lombok `@RequiredArgsConstructor`.
- DTO validation with Jakarta Bean Validation.
- Global exception handling for consistent API errors.
- Transactional write operations for consistency.
- Centralised WebClient builder and environment-driven AI configuration.
- Static bundle served via Spring Boot—no additional build tooling required.

## 🔄 Extensibility Hooks
- Add new categories by updating enum, categorisation rules, and frontend icon/color maps.
- Replace H2 with a persistent database by changing JDBC properties.
- Extend analytics by enriching `TransactionAnalysisService` and DTOs—front-end already consumes JSON generically.
- Swap AI provider by implementing an alternative `*Service` and wiring into `TransactionCategorizationService`/`AIRecommendationEngine`.

---

This architecture keeps the codebase focused on the budgeting experience while remaining easy to extend with future features (budgets, alerts, user auth, etc.).*** End Patch
