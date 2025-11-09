# 🏗️ Hack-Cash Financial Planner - Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         HACK-CASH ECOSYSTEM                                  │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────┐              ┌──────────────────────────────────┐
│   Main Wallet Service    │              │   Financial Planner Service      │
│      (Port 8080)         │              │       (Port 8081)                │
│                          │              │                                  │
│  ┌──────────────────┐   │              │   ┌─────────────────────────┐   │
│  │  User Management │   │              │   │   REST API Controller   │   │
│  │  Wallet CRUD     │   │              │   │   /api/v1/*            │   │
│  │  Transactions    │───┼──JSON File───┼──▶│   - Transactions       │   │
│  │                  │   │   Export     │   │   - Analysis           │   │
│  └──────────────────┘   │              │   │   - Recommendations    │   │
│                          │              │   │   - Dashboard          │   │
└──────────────────────────┘              │   └───────────┬─────────────┘   │
                                          │               │                  │
                                          │               ▼                  │
                                          │   ┌─────────────────────────┐   │
                                          │   │   Service Layer         │   │
                                          │   ├─────────────────────────┤   │
                                          │   │ • TransactionService    │   │
                                          │   │ • AnalysisService       │   │
                                          │   │ • CategorizationService │   │
                                          │   │ • AIRecommendation      │   │
                                          │   └───────────┬─────────────┘   │
                                          │               │                  │
                      ┌───────────────────┼───────────────┼──────────┐      │
                      │                   │               │          │      │
                      ▼                   │               ▼          ▼      │
          ┌──────────────────┐            │   ┌──────────────┐  ┌────────┐│
          │  OpenAI API      │            │   │  Repository  │  │  Cache ││
          │  (GPT-4)         │            │   │   Layer      │  │        ││
          ├──────────────────┤            │   ├──────────────┤  └────────┘│
          │ • Categorization │◀───────────┼───│ • JPA Queries│             │
          │ • Recommendations│            │   │ • Aggregates │             │
          └──────────────────┘            │   └──────┬───────┘             │
                 ▲                        │          │                      │
                 │ Fallback               │          ▼                      │
                 │  if API                │   ┌──────────────┐             │
                 │  fails                 │   │  H2 Database │             │
          ┌──────┴──────────┐             │   │  (In-Memory) │             │
          │  Rule-Based     │             │   └──────────────┘             │
          │  Categorization │             │                                 │
          │  (Regex Patterns)│            │   ┌─────────────────────────┐  │
          └─────────────────┘             │   │   Frontend Dashboard    │  │
                                          │   │   (Static HTML/CSS/JS)  │  │
                                          │   ├─────────────────────────┤  │
                                          │   │ • Chart.js Visualization│  │
                                          │   │ • Real-time Updates     │  │
                                          │   │ • Responsive Design     │  │
                                          │   └─────────────────────────┘  │
                                          └──────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL SERVICES                                    │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────────────────┐
                    │    Kubernetes Cluster (Future)   │
                    │                                  │
                    │  ┌────────────┐  ┌────────────┐ │
                    │  │  Pod 1     │  │  Pod 2     │ │
                    │  │  FP Service│  │  FP Service│ │
                    │  └────────────┘  └────────────┘ │
                    │          │              │        │
                    │          └──────┬───────┘        │
                    │                 ▼                │
                    │        ┌─────────────────┐       │
                    │        │  Load Balancer  │       │
                    │        └─────────────────┘       │
                    │                 │                │
                    │                 ▼                │
                    │        ┌─────────────────┐       │
                    │        │ Health Endpoint │       │
                    │        │ /api/v1/health  │       │
                    │        └─────────────────┘       │
                    └──────────────────────────────────┘
```

---

## 🔄 Data Flow

### **1. Transaction Creation Flow**
```
User Input (Frontend)
    ↓
REST API (/api/v1/transactions)
    ↓
TransactionService
    ↓
┌─→ TransactionCategorizationService
│       ↓
│   OpenAI API (Primary)
│       ↓ (if fails)
│   Rule-Based Fallback
│       ↓
└─ Category Assigned
    ↓
Transaction Entity Saved
    ↓
Database (H2)
    ↓
Response to Frontend
    ↓
Dashboard Updated
```

### **2. Analysis & Recommendations Flow**
```
Dashboard Request (/api/v1/dashboard)
    ↓
┌────────────────────┬──────────────────────┐
│                    │                      │
▼                    ▼                      ▼
AnalysisService   RecommendationEngine  RecentTransactions
    ↓                    ↓                      ↓
Repository Queries   OpenAI API (GPT-4)    Repository Query
    ↓                    ↓ (if fails)          ↓
Aggregations        Rule-Based Logic       Recent Records
    ↓                    ↓                      ↓
Category Stats      AI Insights            Transaction List
    │                    │                      │
    └────────────────────┴──────────────────────┘
                         ↓
                 Combined Dashboard
                         ↓
                 Cached (5 min TTL)
                         ↓
                    JSON Response
                         ↓
                  Frontend Renders
```

### **3. JSON Import Flow (Startup)**
```
Application Startup
    ↓
DataInitializer (PostConstruct)
    ↓
Load JSON File (wallet-transactions.json)
    ↓
Parse Transactions (Flexible Format)
    ↓
For Each Transaction:
    ↓
    TransactionCategorizationService
        ↓
    OpenAI Categorization (if enabled)
        ↓
    Save to Database
    ↓
Log: "Imported X transactions"
```

---

## 🎯 Key Architectural Decisions

### **1. Microservices Pattern**
- **Reason**: Independent deployment, scalability, and maintenance
- **Implementation**: Separate port (8081), own database, REST APIs

### **2. AI-First with Fallback**
- **Reason**: Reliability without compromising intelligence
- **Implementation**: OpenAI primary, rule-based fallback

### **3. Stateless Design**
- **Reason**: Horizontal scalability for cloud deployment
- **Implementation**: No session storage, JWT-ready architecture

### **4. Cache Layer**
- **Reason**: Reduce API calls and improve response time
- **Implementation**: Spring Cache with 5-minute TTL

### **5. Event-Driven Ready**
- **Reason**: Future integration with message queues
- **Implementation**: Service layer abstractions, async processing support

---

## 🔒 Security Layers (Production)

```
┌──────────────────────────────────────┐
│  API Gateway / Load Balancer         │
│  • Rate Limiting                     │
│  • DDoS Protection                   │
└──────────────┬───────────────────────┘
               ▼
┌──────────────────────────────────────┐
│  Authentication / Authorization      │
│  • JWT Tokens                        │
│  • OAuth2 (Future)                   │
└──────────────┬───────────────────────┘
               ▼
┌──────────────────────────────────────┐
│  Financial Planner Service           │
│  • Input Validation                  │
│  • SQL Injection Prevention          │
│  • CORS Configuration                │
└──────────────┬───────────────────────┘
               ▼
┌──────────────────────────────────────┐
│  Database Layer                      │
│  • Encrypted Connections             │
│  • Access Control                    │
└──────────────────────────────────────┘
```

---

## 📊 Scalability Plan

### **Horizontal Scaling**
```
┌─────────────────────────────────────────────┐
│          Load Balancer                      │
└───────────┬─────────────────────┬───────────┘
            │                     │
    ┌───────▼──────┐      ┌──────▼────────┐
    │  Instance 1  │      │  Instance 2   │
    │  FP Service  │      │  FP Service   │
    └───────┬──────┘      └──────┬────────┘
            │                     │
            └──────────┬──────────┘
                       ▼
            ┌──────────────────┐
            │  Shared Database │
            │  (PostgreSQL)    │
            └──────────────────┘
```

### **Caching Strategy**
- **L1 Cache**: In-memory Spring Cache (5 min TTL)
- **L2 Cache** (Future): Redis for distributed caching
- **API Response Cache**: 80% hit rate target

---

## 🚀 Deployment Pipeline

```
Developer → Git Push → GitHub
                ↓
            CI/CD Pipeline
                ↓
        ┌───────────────────┐
        │  Maven Build      │
        │  Unit Tests       │
        │  Integration Tests│
        └────────┬──────────┘
                 ▼
        ┌───────────────────┐
        │  Docker Build     │
        │  Container Image  │
        └────────┬──────────┘
                 ▼
        ┌───────────────────┐
        │  Push to Registry │
        │  (Docker Hub)     │
        └────────┬──────────┘
                 ▼
        ┌───────────────────┐
        │  Deploy to K8s    │
        │  Rolling Update   │
        └────────┬──────────┘
                 ▼
        ┌───────────────────┐
        │  Health Check     │
        │  Smoke Tests      │
        └───────────────────┘
```

---

## 🎯 Monitoring & Observability

```
┌─────────────────────────────────────┐
│  Application Metrics                │
│  • Request Count                    │
│  • Response Times                   │
│  • Error Rates                      │
│  • Cache Hit Ratio                  │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  Health Endpoints                   │
│  /api/v1/health                     │
│  /actuator/health                   │
│  /actuator/metrics                  │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  Monitoring Tools (Future)          │
│  • Prometheus                       │
│  • Grafana Dashboards               │
│  • Alert Manager                    │
└─────────────────────────────────────┘
```

---

**Built for Scale, Performance, and Reliability** 🚀
