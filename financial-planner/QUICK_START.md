# ⚡ Quick Setup Guide - Hack-Cash Financial Planner

## 🚀 Get Started in 3 Steps

### Step 1: Navigate to Project
```bash
cd financial-planner-service
```

### Step 2: Run the Service
```bash
# Option A: Use the startup script (recommended)
./start.sh

# Option B: Manual Maven command
mvn clean install && mvn spring-boot:run
```

### Step 3: Open Dashboard
Open your browser to:
```
http://localhost:8081/index.html
```

---

## ✅ Verify Everything Works

1. **Check Health**
   ```bash
   curl http://localhost:8081/api/v1/health
   ```
   Should return: `{"status":"UP",...}`

2. **Test Dashboard**
   - You should see sample transactions loaded
   - Charts should display spending breakdown
   - AI recommendations should appear

3. **Add Test Transaction**
   - Fill in the form at the bottom
   - Merchant: "Starbucks Coffee"
   - Amount: 12.50
   - Click "Add Transaction"
   - Watch AI categorize it!

---

## 📊 Demo the AI

### Test AI Categorization
Try these merchant names to see AI in action:
- "Whole Foods Market" → GROCERIES
- "Netflix" → ENTERTAINMENT
- "Uber" → TRANSPORT
- "Planet Fitness" → HEALTH
- "Amazon" → SHOPPING

### View AI Recommendations
Check the "AI-Powered Recommendations" section for:
- Spending pattern analysis
- Savings opportunities
- Budget optimization tips

---

## 🔧 Configuration

The OpenAI API key is already configured in:
```
src/main/resources/application.properties
```

If you need to update it:
```properties
openai.api.key=YOUR_NEW_KEY
```

---

## 📁 Project Structure Overview

```
financial-planner-service/
├── src/main/
│   ├── java/          # Backend Spring Boot code
│   └── resources/
│       ├── static/    # Frontend (HTML, CSS, JS)
│       └── wallet-transactions.json  # Sample data
├── pom.xml           # Maven dependencies
├── README.md         # Full documentation
├── DEMO_SCRIPT.md    # Hackathon presentation guide
└── start.sh          # Quick startup script
```

---

## 🎯 Key Features to Demo

1. **Real AI Integration** 
   - OpenAI GPT-4 categorization
   - Intelligent recommendations

2. **Analytics Dashboard**
   - Spending breakdown by category
   - Trend analysis
   - Savings rate calculation

3. **Production Architecture**
   - Microservices design (port 8081)
   - REST APIs
   - Health monitoring
   - Fallback logic

---

## 🐛 Troubleshooting

### Service won't start?
- Check if port 8081 is free: `lsof -i :8081`
- Ensure Java 17+ is installed: `java -version`

### No transactions showing?
- Check if sample data loaded: Check logs for "Successfully imported X transactions"
- Manually trigger import: `curl -X POST http://localhost:8081/api/v1/import/trigger`

### AI not working?
- Check OpenAI API key in application.properties
- Verify internet connection
- Service will use fallback categorization if API fails

---

## 📚 Next Steps

1. **Read Full Docs**: Check `README.md` for complete documentation
2. **Review Demo Script**: See `DEMO_SCRIPT.md` for hackathon presentation tips
3. **Explore APIs**: Test endpoints with Postman or curl
4. **Customize**: Add your own transaction categories or rules

---

## 🎉 You're Ready!

The Financial Planner microservice is now running and ready to impress the judges!

**Dashboard**: http://localhost:8081/index.html  
**API**: http://localhost:8081/api/v1

Good luck with your hackathon! 🚀
