# 💰 Financial Planner – OpenAI-Powered Budget Tracker

Production-ready personal finance dashboard built with **Spring Boot 3.5** and vanilla JavaScript. Automatically categorises spending, tracks weekly/monthly trends, and delivers AI-generated savings tips driven by the **OpenAI Chat Completions API**. A rule-based engine guarantees consistent categorisation when AI is unavailable.

## 🚀 Core Features
- **Automatic Categorisation** – rule/AI hybrid assigns every transaction to Food, Travel, Bills, Entertainment, Shopping, Health, Transport, or Other.
- **Period Analytics** – weekly, monthly, and yearly summaries with per-category percentages, historic deltas, and pie-chart visualisation.
- **AI Savings Guidance** – OpenAI prompts combine benchmark thresholds and period-over-period trends to produce personalised recommendations (e.g. “Food is 40% of spend—10% above target. Cutting 20% frees $110/month.”).
- **Responsive UI** – lightweight HTML/CSS/JS frontend served from Spring Boot (`/static`) with Chart.js visualisations.
- **Clean Spring Boot Architecture** – layered controllers, services, repositories, DTOs, global exception handling, and test scaffold.

## 📋 Prerequisites
- **Java 19+**
- **Maven 3.9+**
- Modern browser (Chrome / Edge / Firefox / Safari)
- Optional: **OpenAI API key** (`OPENAI_API_KEY`) for live AI features

## ⚙️ Configuration & Run

1. **Clone & Install**
```bash
   mvn clean install
   ```
2. **Configure OpenAI (optional but recommended)**
   ```properties
   # src/main/resources/application.properties
   ai.openai.api-key=your-openai-key
   ai.openai.model=gpt-4o-mini
   ```
   or export `OPENAI_API_KEY`.

3. **Start the app**
```bash
   mvn spring-boot:run
   ```
4. **Open the dashboard**
   ```
   http://localhost:5000
   ```

Without an API key the system provides deterministic, benchmark-driven fallback recommendations.

## 📚 REST API Overview
Base URL: `http://localhost:5000/api/v1/transactions`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/` | Create transaction (auto-categorised) |
| `GET`  | `/` | List all transactions |
| `GET`  | `/{id}` | Retrieve single transaction |
| `GET`  | `/category/{category}` | Filter by category (enum name) |
| `DELETE` | `/{id}` | Remove transaction |
| `GET`  | `/analysis?period=monthly` | Summary with category breakdown and AI recs |
| `GET`  | `/stats/{category}?period=monthly` | Deep-dive for one category |
| `GET`  | `/ai/status` | Check OpenAI availability |
| `POST` | `/ai/test` | Quick prompt test against OpenAI |
| `GET`  | `/ai/insights?period=monthly` | Single-sentence spending insight |
| `POST` | `/ai/predict-category` | Predict category before saving |
| `GET`  | `/ai/tips` | Short general-purpose financial tips |
| `GET`  | `/health` | Lightweight service health check |

API responses use `TransactionDTO` projections so the frontend never touches entity objects directly.

## 🧠 Categorisation & Recommendations

- **Categorisation Flow**
  1. Attempt OpenAI classification (`categorizeTransaction`).
  2. Validate category output.
  3. Fallback to curated regex rules (case-insensitive merchant/description matching).

- **Recommendation Engine**
  - Benchmarks per category (Food 30%, Transport 15%, Bills 25%, etc.).
  - Historical comparison (previous period totals and percentage delta).
  - AI prompt summarises totals, variance vs. target, and trend data.
  - Fallback recommendations compute 20% savings targets for categories exceeding thresholds or rising >10% period-over-period.

## 🏗️ Code Structure
```
src/main/java/com/financialplanner
├── FinancialPlannerApplication.java   # Spring Boot entry point
├── controller/
│   └── TransactionController.java     # REST endpoints
├── dto/
│   └── TransactionDTO.java            # Request/response models
├── model/
│   └── Transaction.java               # JPA entity + enum categories
├── repository/
│   └── TransactionRepository.java     # JPA queries & aggregates
├── service/
│   ├── TransactionService.java        # CRUD + auto categorisation
│   ├── TransactionAnalysisService.java# Period analytics, trend calc
│   ├── TransactionCategorizationService.java
│   ├── AIRecommendationEngine.java    # OpenAI + fallback logic
│   └── OpenAIService.java             # HTTP client wrapper
├── config/
│   ├── DataInitializer.java           # Sample seed data
│   └── WebClientConfig.java           # Shared WebClient builder
└── exception/
    └── GlobalExceptionHandler.java    # Consistent error payloads

src/main/resources/static
├── index.html
├── styles.css
└── app.js
```

## 🛡️ Engineering Practices
- Constructor injection via Lombok’s `@RequiredArgsConstructor`.
- Validation annotations on DTOs, handled globally.
- Transactional boundaries on write operations.
- Structured logging with SLF4J.
- Rule-of-three friendly services (easy to extend categories or analytics).
- H2 in-memory DB for local use; swap JDBC URL for production.

## 🧪 Development Notes
- Seed data inserted at startup for demo dashboards.
- H2 console available at `/h2-console` (when enabled).
- Static assets served directly by Spring Boot—no build step required.

## 🗺️ Roadmap Ideas
- Multi-user support & authentication.
- Budget target tracking with alerts.
- Export to CSV/PDF and scheduled reports.
- Bank API integrations & recurring transaction detection.

---

**Happy budgeting!** Add your expenses, compare trends, and let OpenAI highlight the biggest savings opportunities. 💡💰📊
