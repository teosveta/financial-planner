# 🚀 Quick Start Guide

## Option 1: Quick Start (Recommended)

### On Mac/Linux:
```bash
cd financial-planner
chmod +x start.sh
./start.sh
```

### On Windows:
```cmd
cd financial-planner
start.bat
```

## Option 2: Manual Start

1. **Build the project:**
   ```bash
   ./mvnw clean install
   ```

2. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Open your browser:**
   ```
   http://localhost:8080
   ```

## 🎯 What to Do Next

1. **Explore the Dashboard** - See sample transactions and AI recommendations
2. **Add Transactions** - Click "Add Transaction" tab and create your own
3. **View Categories** - Watch automatic categorization in action
4. **Check Recommendations** - Get personalized savings advice

## 📱 Key Features to Try

- ✅ Add a transaction with "Starbucks" → Auto-categorized as Food
- ✅ Add "Uber" → Auto-categorized as Transport
- ✅ Switch between Weekly/Monthly/Yearly views
- ✅ Delete a transaction and see real-time updates
- ✅ Check the pie chart visualization

## 🔧 Technical Details

- **Backend**: Spring Boot 3.5.7, Java 21
- **Database**: H2 (in-memory, resets on restart)
- **Frontend**: Vanilla JavaScript, Chart.js
- **Port**: 8080
- **API**: http://localhost:8080/api/v1/transactions

## 💡 Tips for Developers

### View Database Console:
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:financialdb`
- Username: `sa`
- Password: (empty)

### Test API Endpoints:
```bash
# Get all transactions
curl http://localhost:8080/api/v1/transactions

# Get analysis
curl http://localhost:8080/api/v1/transactions/analysis?period=monthly

# Add transaction
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"merchantName":"Starbucks","description":"Coffee","amount":5.99}'
```

### Stop the Application:
Press `Ctrl+C` in the terminal

## 🎓 Learning Points

This project demonstrates:
- ✅ Spring Boot REST API best practices
- ✅ Service layer architecture
- ✅ Repository pattern with JPA
- ✅ DTO pattern for API responses
- ✅ Rule-based AI recommendations
- ✅ Frontend-backend integration
- ✅ Exception handling and validation
- ✅ Transaction management

## 🆘 Troubleshooting

**Port 8080 already in use?**
- Change port in `application.properties`: `server.port=8081`

**Java version error?**
- Ensure Java 21+ is installed: `java -version`

**Build fails?**
- Clean and retry: `./mvnw clean install -U`

---

Need help? Check the full [README.md](README.md) for complete documentation!
