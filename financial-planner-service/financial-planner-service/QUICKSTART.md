# 🚀 Quick Start Guide - Financial Planner

## ⚡ 5-Minute Setup

### Step 1: Navigate to the project
```bash
cd financial-planner-service
```

### Step 2: Run setup script
```bash
chmod +x setup.sh
./setup.sh
```

### Step 3: Start the service
```bash
./run.sh
```

### Step 4: Open the dashboard
Navigate to: `http://localhost:8081/index.html`

---

## 🎯 Quick Test

### Using the Dashboard
1. Open `http://localhost:8081/index.html`
2. Click **"Categorize All"** (uses real AI!)
3. Wait ~10 seconds for OpenAI to process
4. Click **"Analyze"**
5. View your financial insights!

### Using API
```bash
# Test health endpoint
curl http://localhost:8081/api/v1/financial-planner/health

# Categorize transactions
curl -X POST http://localhost:8081/api/v1/financial-planner/categorize-all

# Get analysis
curl "http://localhost:8081/api/v1/financial-planner/analysis/123e4567-e89b-12d3-a456-426614174000?period=month"
```

---

## 📊 What You'll See

### AI Categorization
- Real OpenAI API calls analyzing each transaction
- Categories: Food, Travel, Bills, Entertainment, Shopping, Health, Transport, etc.
- Confidence scores for each categorization

### Financial Analysis
- **Summary**: Total spent, income, savings
- **Breakdown**: Spending by category with percentages
- **Chart**: Interactive pie chart visualization
- **Recommendations**: AI-generated savings advice
- **Insights**: Natural language spending insights

---

## 🎤 For Hackathon Demo

### Before Demo
1. Start service: `./run.sh`
2. Test API: `./test-api.sh`
3. Open dashboard: `http://localhost:8081/index.html`
4. Have code ready: `src/main/java/.../service/AICategorizationService.java`

### During Demo
1. **Show Dashboard** - Click buttons, display results
2. **Show Code** - Actual OpenAI API integration
3. **Explain Value** - Real AI, actionable insights, production-ready

### Talking Points
✅ "Real AI using OpenAI GPT-4o-mini"
✅ "Not hardcoded rules - actual machine learning"
✅ "Actionable recommendations with specific numbers"
✅ "Production-ready microservices architecture"
✅ "Kubernetes-ready with health monitoring"

---

## 🐛 Common Issues

### Port 8081 in use
```bash
# Kill process on port 8081
lsof -i :8081
kill -9 <PID>
```

### OpenAI API errors
- Check internet connection
- Verify API key in `src/main/resources/application.yml`
- Check OpenAI service status

### No transactions found
- Ensure `data/transactions.json` exists
- Verify JSON is valid: `cat data/transactions.json | jq`
- Check user ID matches: `123e4567-e89b-12d3-a456-426614174000`

---

## 📁 Project Structure

```
financial-planner-service/
├── src/main/java/com/paysafe/hackcash/financialplanner/
│   ├── FinancialPlannerServiceApplication.java   # Main app
│   ├── controller/
│   │   └── FinancialPlannerController.java       # REST API
│   ├── service/
│   │   ├── AICategorizationService.java          # OpenAI integration
│   │   ├── AIRecommendationService.java          # AI recommendations
│   │   └── FinancialAnalysisService.java         # Main logic
│   ├── repository/
│   │   └── TransactionRepository.java            # Data access
│   ├── model/                                    # Domain models
│   ├── dto/                                      # API responses
│   └── config/                                   # Configuration
├── src/main/resources/
│   ├── application.yml                           # Config
│   └── static/
│       └── index.html                            # Dashboard
├── data/
│   └── transactions.json                         # Sample data
├── README.md                                     # Full documentation
├── DEMO_SCRIPT.md                                # Presentation guide
├── setup.sh                                      # Setup script
├── run.sh                                        # Run script
└── test-api.sh                                   # API test script
```

---

## 🔑 Key Features

1. **Real AI Integration**
   - Actual OpenAI API calls
   - GPT-4o-mini model
   - Structured JSON responses

2. **Smart Categorization**
   - 10 spending categories
   - Confidence scoring
   - Batch processing

3. **AI Recommendations**
   - Specific savings amounts
   - Priority levels
   - Actionable advice

4. **Production Ready**
   - Spring Boot microservice
   - Health monitoring
   - Error handling
   - Docker support

---

## 📞 Need Help?

1. Check README.md for detailed info
2. Review logs: `tail -f logs/application.log`
3. Test APIs: `./test-api.sh`
4. Verify service: `curl http://localhost:8081/api/v1/financial-planner/health`

---

## 🏆 Success Checklist

Before demo:
- [ ] Service is running on port 8081
- [ ] Dashboard loads at `http://localhost:8081/index.html`
- [ ] Health endpoint returns 200 OK
- [ ] Sample transactions are loaded
- [ ] OpenAI API key is configured
- [ ] "Categorize All" works (real AI)
- [ ] "Analyze" displays results
- [ ] Pie chart renders correctly

---

**You're all set! Time to impress those judges! 🎉**

For detailed information, see:
- **README.md** - Full documentation
- **DEMO_SCRIPT.md** - Presentation guide
