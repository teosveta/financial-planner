#!/bin/bash
####################################################################
# Test JSON Import Functionality
# This script tests the JSON import feature with example files
####################################################################

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo "============================================"
echo "  Testing JSON Import Functionality"
echo "============================================"
echo ""

# Check if server is running
if ! curl -s http://localhost:8080/api/v1/import/health > /dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} Financial Planner is not running!"
    echo ""
    echo "Please start the application first:"
    echo "  ./mvnw spring-boot:run"
    echo ""
    exit 1
fi

echo -e "${GREEN}[OK]${NC} Server is running"
echo ""

# Test 1: Import example-simple.json
echo "============================================"
echo "Test 1: Import Simple Format"
echo "============================================"
echo "File: example-simple.json"
echo ""

curl -X POST http://localhost:8080/api/v1/import/file \
  -F "file=@example-simple.json" \
  -H "Accept: application/json"

echo ""
echo ""
sleep 2

# Test 2: Import example-import.json
echo "============================================"
echo "Test 2: Import Full Format"
echo "============================================"
echo "File: example-import.json"
echo ""

curl -X POST http://localhost:8080/api/v1/import/file \
  -F "file=@example-import.json" \
  -H "Accept: application/json"

echo ""
echo ""
sleep 2

# Test 3: Get example format
echo "============================================"
echo "Test 3: Get Example Format"
echo "============================================"
echo ""

curl http://localhost:8080/api/v1/import/example

echo ""
echo ""
sleep 2

# Test 4: Check analysis after import
echo "============================================"
echo "Test 4: Check Analysis After Import"
echo "============================================"
echo ""

curl http://localhost:8080/api/v1/transactions/analysis?period=monthly

echo ""
echo ""

echo "============================================"
echo "  All Tests Completed!"
echo "============================================"
echo ""
echo "Check the dashboard: http://localhost:8080"
echo ""
echo "You should see:"
echo "  - Imported transactions"
echo "  - Automatic categorization"
echo "  - AI analysis and recommendations"
echo ""

