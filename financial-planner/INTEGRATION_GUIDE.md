# 🔗 Integration Guide: Financial Planner ↔ Main Wallet

## 📋 Overview

This guide explains how to integrate the Financial Planner microservice with your main Hack-Cash Digital Wallet application.

---

## 🎯 Integration Options

### **Option 1: JSON File Export/Import** (Current - Easiest)
### **Option 2: REST API Communication** (Recommended)
### **Option 3: Shared Database** (Advanced)
### **Option 4: Event-Driven (Kafka)** (Future)

---

## 🔧 Option 1: JSON File Export/Import (CURRENT)

### **How It Works**
```
Main Wallet (8080) → Exports JSON → Financial Planner (8081) → Imports
```

### **Step 1: Export Transactions from Main Wallet**

Add this endpoint to your main wallet service:

```java
@RestController
@RequestMapping("/api/export")
public class ExportController {
    
    @Autowired
    private TransactionService transactionService;
    
    @GetMapping("/transactions")
    public ResponseEntity<String> exportTransactions() {
        List<Transaction> transactions = transactionService.findAll();
        
        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Map.of("transactions", transactions));
        
        return ResponseEntity.ok(json);
    }
}
```

### **Step 2: Save to File**

```java
@Service
public class ExportService {
    
    public void exportToFile() {
        String json = // get from API
        Files.write(
            Paths.get("wallet-transactions.json"), 
            json.getBytes()
        );
    }
}
```

### **Step 3: Financial Planner Auto-Imports**

The Financial Planner automatically imports on startup from:
```
src/main/resources/wallet-transactions.json
```

**That's it!** No code changes needed in Financial Planner.

---

## 🚀 Option 2: REST API Communication (RECOMMENDED)

### **How It Works**
```
Main Wallet (8080) → REST API calls → Financial Planner (8081)
                   ← JSON responses ←
```

### **Step 1: Add OpenFeign Client to Main Wallet**

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### **Step 2: Create Feign Client**

```java
@FeignClient(name = "financial-planner", url = "http://localhost:8081")
public interface FinancialPlannerClient {
    
    @PostMapping("/api/v1/transactions")
    TransactionDTO createTransaction(@RequestBody TransactionDTO dto);
    
    @GetMapping("/api/v1/analysis")
    SpendingAnalysisDTO getAnalysis(
        @RequestParam int period, 
        @RequestParam String unit
    );
    
    @GetMapping("/api/v1/recommendations")
    List<RecommendationDTO> getRecommendations(
        @RequestParam int period, 
        @RequestParam String unit
    );
}
```

### **Step 3: Use in Main Wallet Service**

```java
@Service
public class WalletService {
    
    @Autowired
    private FinancialPlannerClient financialPlannerClient;
    
    public void createTransaction(TransactionDTO dto) {
        // Create in main wallet
        Transaction txn = save(dto);
        
        // Send to financial planner asynchronously
        CompletableFuture.runAsync(() -> {
            financialPlannerClient.createTransaction(dto);
        });
    }
}
```

### **Step 4: Display in Main Wallet UI**

```java
@GetMapping("/wallet/insights")
public String showInsights(Model model) {
    SpendingAnalysisDTO analysis = 
        financialPlannerClient.getAnalysis(1, "month");
    
    List<RecommendationDTO> recommendations = 
        financialPlannerClient.getRecommendations(1, "month");
    
    model.addAttribute("analysis", analysis);
    model.addAttribute("recommendations", recommendations);
    
    return "insights";
}
```

---

## 💾 Option 3: Shared Database (ADVANCED)

### **How It Works**
```
Main Wallet → Writes to DB ← Financial Planner reads from DB
```

### **Step 1: Configure Shared PostgreSQL**

**Main Wallet application.properties:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hackcash
spring.datasource.username=postgres
spring.datasource.password=password
```

**Financial Planner application.properties:**
```properties
# Same database, read-only mode
spring.datasource.url=jdbc:postgresql://localhost:5432/hackcash
spring.jpa.properties.hibernate.default_schema=public
spring.jpa.hibernate.ddl-auto=validate  # Read-only!
```

### **Step 2: Map to Same Tables**

Ensure entity names match:
```java
// Main Wallet
@Entity
@Table(name = "transactions")
public class Transaction { ... }

// Financial Planner (same table name!)
@Entity
@Table(name = "transactions")
public class Transaction { ... }
```

### **Step 3: Financial Planner Auto-Detects New Data**

No code changes needed! Repository queries work automatically.

---

## 📨 Option 4: Event-Driven (KAFKA) - Future

### **How It Works**
```
Main Wallet → Publishes event → Kafka → Financial Planner subscribes
```

### **Step 1: Add Kafka Dependencies**

Both services need:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### **Step 2: Main Wallet Publishes Events**

```java
@Service
public class TransactionEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, TransactionDTO> kafkaTemplate;
    
    public void publishTransaction(Transaction txn) {
        TransactionDTO dto = toDTO(txn);
        kafkaTemplate.send("transaction-events", dto);
    }
}
```

### **Step 3: Financial Planner Subscribes**

```java
@Service
public class TransactionEventConsumer {
    
    @Autowired
    private TransactionService transactionService;
    
    @KafkaListener(topics = "transaction-events")
    public void consume(TransactionDTO dto) {
        transactionService.createTransaction(dto);
    }
}
```

---

## 🎯 Recommended Approach for Hackathon

### **For Demo:**
Use **Option 1 (JSON)** - Simple, works immediately

### **For Production:**
Use **Option 2 (REST API)** - Standard microservices pattern

### **Future Enhancement:**
Add **Option 4 (Kafka)** - Show scalability thinking

---

## 📊 Data Flow Comparison

### **JSON Import**
```
Main Wallet → Export JSON file → Financial Planner imports on startup
Pros: Simple, no network calls
Cons: Manual process, not real-time
```

### **REST API**
```
Main Wallet → HTTP POST → Financial Planner processes immediately
Pros: Real-time, automatic, standard
Cons: Network dependency
```

### **Shared Database**
```
Main Wallet → Writes to DB → Financial Planner reads from DB
Pros: No duplication, consistent data
Cons: Tight coupling
```

### **Event-Driven**
```
Main Wallet → Kafka event → Financial Planner processes async
Pros: Loose coupling, scalable, resilient
Cons: More complex setup
```

---

## 🔧 Quick Integration for Demo

### **1. Add Export Endpoint to Main Wallet**

```java
@GetMapping("/api/export/transactions")
public void exportTransactions() throws IOException {
    List<Transaction> transactions = transactionRepository.findAll();
    
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(
        Map.of("transactions", transactions)
    );
    
    Files.write(
        Paths.get("../financial-planner-service/src/main/resources/wallet-transactions.json"),
        json.getBytes()
    );
}
```

### **2. Call Before Demo**

```bash
curl http://localhost:8080/api/export/transactions
```

### **3. Restart Financial Planner**

```bash
cd financial-planner-service
./start.sh
```

**Done!** Your transactions are now analyzed with AI.

---

## 🎤 Demo Script for Integration

### **Show to Judges:**

"Our Financial Planner is a true microservice - it integrates with the main wallet through multiple methods..."

**Point 1: JSON Import** (Current)
"For rapid development, we use JSON export/import. Watch..."
- Show export endpoint
- Show imported data in dashboard

**Point 2: REST APIs** (Production-ready)
"For production, we have REST APIs ready..."
- Show Feign client code
- Explain async communication

**Point 3: Event-Driven** (Future)
"For scale, we've designed for Kafka events..."
- Show architecture diagram
- Explain loose coupling benefits

---

## ✅ Integration Checklist

Before hackathon demo:

- [ ] Main wallet can export transactions to JSON
- [ ] JSON file is in correct location
- [ ] Financial Planner imports successfully
- [ ] Data shows in dashboard
- [ ] Charts render correctly
- [ ] AI categorization works
- [ ] Recommendations generate

---

## 🔍 Verify Integration

### **Test 1: Data Transfer**
```bash
# Check main wallet has transactions
curl http://localhost:8080/api/transactions | jq length

# Export to file
curl http://localhost:8080/api/export/transactions

# Check financial planner received them
curl http://localhost:8081/api/v1/transactions | jq length
```

### **Test 2: Real-time Sync (if using REST API)**
```bash
# Add transaction in main wallet
curl -X POST http://localhost:8080/api/transactions -d {...}

# Should appear in financial planner
curl http://localhost:8081/api/v1/transactions/recent
```

---

## 🐛 Troubleshooting Integration

### **Transactions not appearing?**
1. Check export endpoint works
2. Verify JSON file location
3. Check file permissions
4. Restart financial planner
5. Check import logs

### **Data format mismatch?**
Financial Planner supports flexible formats:
- `merchantName` / `merchant` / `sender`
- `description` / `details` / `memo`
- `amount` / `value`
- `transactionDate` / `date` / `timestamp`

### **Network errors?**
- Check both services running
- Verify ports (8080, 8081)
- Check firewall settings
- Test with curl

---

## 📚 Additional Resources

- **Main Wallet Code**: (Your existing code)
- **Financial Planner API**: See `README.md`
- **OpenFeign Docs**: https://spring.io/projects/spring-cloud-openfeign
- **Kafka Integration**: (For future reference)

---

## 🎉 Success!

When integrated properly, you'll have:

✅ Main wallet manages transactions  
✅ Financial Planner provides AI insights  
✅ Both work independently  
✅ Data flows seamlessly  
✅ Production-ready architecture  

**Your microservices ecosystem is complete!** 🚀

---

**Questions? Check the README.md or DEMO_SCRIPT.md**
