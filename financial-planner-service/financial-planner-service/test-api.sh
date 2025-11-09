#!/bin/bash

echo "=========================================="
echo "🧪 Financial Planner API Test Suite"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8081/api/v1/financial-planner"
USER_ID="123e4567-e89b-12d3-a456-426614174000"

# Test 1: Health Check
echo "Test 1: Health Check"
echo "---"
curl -s "$BASE_URL/health"
echo -e "\n"
sleep 1

# Test 2: Categorize All Transactions
echo "Test 2: Categorize All Transactions (with AI)"
echo "---"
echo "This will use OpenAI to categorize all transactions..."
curl -X POST -s "$BASE_URL/categorize-all"
echo -e "\n"
sleep 2

# Test 3: Get Monthly Analysis
echo "Test 3: Get Monthly Financial Analysis"
echo "---"
curl -s "$BASE_URL/analysis/$USER_ID?period=month" | jq '.'
echo ""
sleep 1

# Test 4: Get Weekly Analysis
echo "Test 4: Get Weekly Financial Analysis"
echo "---"
curl -s "$BASE_URL/analysis/$USER_ID?period=week" | jq '.'
echo ""

echo "=========================================="
echo "✅ API Tests Complete"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Review the results above"
echo "2. Open dashboard: http://localhost:8081/index.html"
echo "3. Check logs: tail -f logs/application.log"
echo ""
