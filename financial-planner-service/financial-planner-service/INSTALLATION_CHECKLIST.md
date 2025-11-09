# ✅ Installation & Verification Checklist

## Pre-Installation Check

### System Requirements
- [ ] Java 21 or higher installed
  ```bash
  java -version
  # Should show: openjdk version "21" or higher
  ```

- [ ] Maven 3.8+ installed
  ```bash
  mvn -version
  # Should show: Apache Maven 3.8 or higher
  ```

- [ ] Internet connection available (for OpenAI API)

- [ ] Port 8081 available
  ```bash
  lsof -i :8081
  # Should return nothing (port is free)
  ```

---

## Installation Steps

### Step 1: Extract Package ✅
```bash
# Navigate to the project directory
cd financial-planner-service

# Verify structure
ls -la
# Should see: src/, data/, pom.xml, README.md, setup.sh, etc.
```

### Step 2: Run Setup ✅
```bash
# Make scripts executable
chmod +x setup.sh run.sh test-api.sh

# Run setup
./setup.sh

# Expected output:
# ✅ Java version OK
# ✅ Maven OK
# ✅ Directories created
# ✅ Build successful
# ✅ OpenAI API key configured
```

### Step 3: Verify Configuration ✅
```bash
# Check OpenAI API key
grep "sk-proj-" src/main/resources/application.yml

# Check sample data
cat data/transactions.json | jq length
# Should return: 12 (number of sample transactions)
```

### Step 4: Start Service ✅
```bash
./run.sh

# Expected output:
# Started FinancialPlannerServiceApplication in X.XXX seconds
# Application is running on port 8081
```

---

## Verification Tests

### Test 1: Health Check ✅
```bash
# In a new terminal
curl http://localhost:8081/api/v1/financial-planner/health

# Expected: "Financial Planner Service is running"
# Status: 200 OK
```

### Test 2: Dashboard Access ✅
```bash
# Open in browser
http://localhost:8081/index.html

# Verify:
# - [ ] Page loads without errors
# - [ ] Dashboard displays properly
# - [ ] Controls are visible (User ID input, Period select, buttons)
```

### Test 3: AI Categorization ✅
```bash
# Via dashboard: Click "Categorize All" button
# OR via API:
curl -X POST http://localhost:8081/api/v1/financial-planner/categorize-all

# Expected: "Categorization completed successfully"
# Status: 200 OK
# Wait time: ~10-15 seconds (real AI processing)
```

### Test 4: Financial Analysis ✅
```bash
# Via dashboard: Enter user ID and click "Analyze"
# OR via API:
curl "http://localhost:8081/api/v1/financial-planner/analysis/123e4567-e89b-12d3-a456-426614174000?period=month" | jq

# Expected: JSON response with:
# - [ ] summary (totalSpent, totalIncome, netSavings)
# - [ ] categoryBreakdown (spending by category)
# - [ ] recommendations (AI-generated advice)
# - [ ] insights (natural language insights)
# - [ ] trends (spending patterns)
```

### Test 5: Visualization ✅
In browser at `http://localhost:8081/index.html`:
- [ ] Summary cards display correctly
- [ ] Pie chart renders with categories
- [ ] AI recommendations section shows
- [ ] Insights cards display
- [ ] All percentages and amounts are accurate

---

## API Test Suite

Run comprehensive tests:
```bash
./test-api.sh

# This will test:
# 1. Health check
# 2. Categorization (with AI)
# 3. Monthly analysis
# 4. Weekly analysis

# All tests should return 200 OK with valid JSON
```

---

## Demo Readiness Check

### Before Hackathon Demo

#### Service Status ✅
- [ ] Service running on port 8081
- [ ] No errors in console logs
- [ ] Health check returns 200 OK

#### Data Readiness ✅
- [ ] transactions.json exists and contains data
- [ ] Sample data has valid user ID: `123e4567-e89b-12d3-a456-426614174000`
- [ ] All transactions have required fields

#### Dashboard Functionality ✅
- [ ] Dashboard loads at `http://localhost:8081/index.html`
- [ ] "Categorize All" button works (real AI processing visible)
- [ ] "Analyze" button displays results
- [ ] Pie chart renders correctly
- [ ] Recommendations display with specific numbers
- [ ] No JavaScript console errors

#### Code Readiness ✅
- [ ] AICategorizationService.java is accessible
- [ ] Can quickly navigate to show OpenAI integration
- [ ] README.md is available for reference
- [ ] DEMO_SCRIPT.md is ready for presentation

#### Presentation Materials ✅
- [ ] DEMO_SCRIPT.md reviewed
- [ ] Talking points memorized
- [ ] Anticipated questions prepared
- [ ] Architecture diagram ready (in README.md)

---

## Troubleshooting Common Issues

### Issue 1: Service won't start
**Symptom:** Error when running `./run.sh`

**Solutions:**
```bash
# Check Java version
java -version  # Must be 21+

# Check port availability
lsof -i :8081
kill -9 <PID>  # If port is in use

# Rebuild
mvn clean install
```

### Issue 2: AI Categorization fails
**Symptom:** "Categorization failed" or all transactions show "OTHER"

**Solutions:**
```bash
# Verify API key
grep "OPENAI_API_KEY" src/main/resources/application.yml

# Check internet connection
ping api.openai.com

# Review logs
tail -f logs/application.log
```

### Issue 3: Dashboard shows no data
**Symptom:** Empty analysis or "No transactions found"

**Solutions:**
```bash
# Check transactions file
cat data/transactions.json | jq

# Verify file path
grep "file-path" src/main/resources/application.yml

# Ensure user ID matches
# In transactions.json: "owner_id": "123e4567-e89b-12d3-a456-426614174000"
# In dashboard: Use same user ID
```

### Issue 4: Chart.js not loading
**Symptom:** Pie chart doesn't display

**Solutions:**
```bash
# Check browser console for errors
# Open Developer Tools (F12) → Console

# Verify internet connection (for CDN)
# Chart.js loads from: https://cdn.jsdelivr.net/npm/chart.js

# Clear browser cache and reload
```

---

## Performance Benchmarks

### Expected Performance

**Categorization Time:**
- 5 transactions: ~3-5 seconds
- 12 transactions (sample): ~10-15 seconds
- 50 transactions: ~30-40 seconds

**Analysis Time:**
- After categorization: <1 second
- Includes AI recommendations: 2-3 seconds

**Memory Usage:**
- Initial: ~200 MB
- Under load: ~400 MB
- Peak: ~600 MB

**API Response Times:**
- Health check: <50ms
- Analysis (cached): <100ms
- Analysis (fresh): 2-3 seconds
- Categorization: 3-15 seconds (varies by count)

---

## Security Verification

### API Key Security ✅
- [ ] API key is in environment variables (production)
- [ ] API key is not in version control
- [ ] .gitignore includes sensitive files

### Input Validation ✅
- [ ] User ID format validated
- [ ] Period parameter validated
- [ ] Transaction data sanitized

### Error Handling ✅
- [ ] No stack traces exposed to users
- [ ] Generic error messages
- [ ] Detailed logs for debugging

---

## Deployment Checklist

### Docker Deployment ✅
```bash
# Build image
docker build -t financial-planner:1.0.0 .

# Run container
docker run -p 8081:8081 \
  -e OPENAI_API_KEY="your-key" \
  financial-planner:1.0.0

# Verify
curl http://localhost:8081/api/v1/financial-planner/health
```

### Docker Compose Deployment ✅
```bash
# Set environment variable
export OPENAI_API_KEY="your-key"

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f

# Verify
curl http://localhost:8081/api/v1/financial-planner/health
```

---

## Final Pre-Demo Checklist

**5 Minutes Before Demo:**

1. Service Running ✅
   ```bash
   curl http://localhost:8081/api/v1/financial-planner/health
   ```

2. Dashboard Ready ✅
   ```bash
   open http://localhost:8081/index.html
   # (or navigate in browser)
   ```

3. Fresh Categorization ✅
   - Click "Categorize All"
   - Wait for completion
   - Verify success message

4. Demo Analysis Ready ✅
   - User ID: `123e4567-e89b-12d3-a456-426614174000`
   - Period: "This Month"
   - Click "Analyze"
   - Verify results display

5. Code Ready ✅
   - IDE open to `AICategorizationService.java`
   - README.md accessible
   - DEMO_SCRIPT.md visible

6. Backup Plan ✅
   - Screenshots of working demo
   - Code examples ready to show
   - API test results saved

---

## Success Indicators

You're ready when:

✅ All services start without errors
✅ Health check returns 200 OK
✅ Dashboard loads and displays data
✅ AI categorization works (real OpenAI calls)
✅ Analysis generates with recommendations
✅ Pie chart renders correctly
✅ No console errors in browser
✅ API tests pass successfully
✅ Demo script is memorized
✅ Code is accessible and understood
✅ Backup materials are prepared

---

## Emergency Contacts

**If demo fails:**
1. Stay calm and explain the architecture
2. Show code instead of running demo
3. Walk through API responses in README.md
4. Highlight technical choices and innovations
5. Emphasize production-readiness

**Key talking points even without demo:**
- Real AI integration (show code)
- Production-ready architecture (show structure)
- Actionable insights (show examples in docs)
- Comprehensive solution (show file tree)

---

## Post-Demo Actions

After successful demo:
- [ ] Thank judges for their time
- [ ] Ask if they have questions
- [ ] Offer to share code/docs
- [ ] Get feedback
- [ ] Exchange contact info

---

**Remember:** You built something real and impressive. Even if technical issues arise during the demo, your code and architecture speak for themselves. Good luck! 🚀
