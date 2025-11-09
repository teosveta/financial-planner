#!/bin/bash

echo "🚀 Starting Financial Planner..."
echo ""
echo "Building the application..."
./mvnw clean install

echo ""
echo "Starting Spring Boot application..."
echo "The app will be available at: http://localhost:8080"
echo ""

./mvnw spring-boot:run
