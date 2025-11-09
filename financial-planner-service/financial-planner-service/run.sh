#!/bin/bash

echo "=========================================="
echo "🚀 Starting Financial Planner Service"
echo "=========================================="
echo ""
echo "Service will start on port 8081"
echo "Dashboard: http://localhost:8081/index.html"
echo "Health: http://localhost:8081/api/v1/financial-planner/health"
echo ""
echo "Press Ctrl+C to stop"
echo ""

# Run the Spring Boot application
mvn spring-boot:run
