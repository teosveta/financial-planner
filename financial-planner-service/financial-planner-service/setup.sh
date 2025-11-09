#!/bin/bash

echo "=========================================="
echo "🚀 Hack-Cash Financial Planner Setup"
echo "=========================================="
echo ""

# Check Java version
echo "Checking Java version..."
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java version: $java_version"

if [[ ! "$java_version" =~ ^(21|[2-9][0-9])\. ]]; then
    echo "❌ Error: Java 21 or higher is required"
    exit 1
fi
echo "✅ Java version OK"
echo ""

# Check Maven
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed"
    exit 1
fi
mvn_version=$(mvn -version | grep "Apache Maven" | awk '{print $3}')
echo "Maven version: $mvn_version"
echo "✅ Maven OK"
echo ""

# Create necessary directories
echo "Creating directories..."
mkdir -p data
mkdir -p logs
mkdir -p src/main/resources/static
echo "✅ Directories created"
echo ""

# Check if transactions file exists
if [ ! -f "data/transactions.json" ]; then
    echo "⚠️  Warning: data/transactions.json not found"
    echo "Creating sample transactions file..."
    
    # Sample data will be created by the build
    echo "✅ Sample data will be available after build"
else
    echo "✅ Transactions file found"
fi
echo ""

# Build the project
echo "Building project..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    exit 1
fi
echo ""

# Check OpenAI API key
echo "Checking OpenAI API configuration..."
if grep -q "sk-proj-" src/main/resources/application.yml; then
    echo "✅ OpenAI API key configured"
else
    echo "⚠️  Warning: OpenAI API key not found in application.yml"
    echo "Please configure your API key before running"
fi
echo ""

echo "=========================================="
echo "✅ Setup Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Review data/transactions.json (sample data provided)"
echo "2. Verify OpenAI API key in src/main/resources/application.yml"
echo "3. Start the service: ./run.sh"
echo "4. Open dashboard: http://localhost:8081/index.html"
echo ""
echo "For more information, see README.md"
echo ""
