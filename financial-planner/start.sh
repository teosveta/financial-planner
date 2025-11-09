#!/bin/bash

echo "🚀 Starting Hack-Cash Financial Planner Service..."
echo ""
echo "📋 Configuration:"
echo "   - Port: 8081"
echo "   - AI: OpenAI GPT-4"
echo "   - Database: H2 (In-Memory)"
echo ""

# Build the project
echo "🔨 Building project..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "🎯 Starting application..."
    echo "   Dashboard: http://localhost:8081/index.html"
    echo "   API: http://localhost:8081/api/v1"
    echo "   Health: http://localhost:8081/api/v1/health"
    echo ""
    
    # Run the application
    mvn spring-boot:run
else
    echo ""
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
