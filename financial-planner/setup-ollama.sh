#!/bin/bash
####################################################################
# Ollama AI Setup Script for Linux/Mac
# This script helps you install and configure Ollama for AI features
####################################################################

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "============================================"
echo "  Financial Planner - AI Setup (Ollama)"
echo "============================================"
echo ""

# Check if Ollama is installed
if command -v ollama &> /dev/null; then
    echo -e "${GREEN}[OK]${NC} Ollama is already installed!"
    echo ""
else
    echo -e "${RED}[!]${NC} Ollama is NOT installed."
    echo ""
    echo "Installing Ollama..."
    echo ""
    
    # Detect OS
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux installation
        curl -fsSL https://ollama.com/install.sh | sh
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS installation
        echo "Please install Ollama from: https://ollama.com/download"
        echo ""
        echo "Or use Homebrew:"
        echo "  brew install ollama"
        echo ""
        echo "After installation, run this script again."
        exit 1
    else
        echo "Unsupported OS: $OSTYPE"
        echo "Please visit: https://ollama.com/download"
        exit 1
    fi
    
    echo ""
    echo -e "${GREEN}Ollama installed successfully!${NC}"
    echo ""
fi

# Check if Ollama service is running
echo "Checking if Ollama service is running..."
sleep 2

if curl -s http://localhost:11434/api/tags &> /dev/null; then
    echo -e "${GREEN}[OK]${NC} Ollama service is running!"
    echo ""
else
    echo -e "${YELLOW}[!]${NC} Ollama service is NOT running."
    echo ""
    echo "Starting Ollama service in background..."
    ollama serve &> /dev/null &
    OLLAMA_PID=$!
    echo "Waiting for service to start..."
    sleep 5
    echo ""
fi

# Check for AI models
echo "Checking for AI models..."
echo ""

if ollama list 2>/dev/null | grep -q "llama3.2"; then
    echo -e "${GREEN}[OK]${NC} Model 'llama3.2' is already installed!"
    echo ""
else
    echo -e "${YELLOW}[!]${NC} Model 'llama3.2' is NOT installed."
    echo ""
    echo "Choose an AI model to download:"
    echo "  1. llama3.2 (Recommended, ~2GB)"
    echo "  2. mistral (High quality, ~4GB)"
    echo "  3. phi3 (Fastest, ~2.3GB)"
    echo "  4. Skip model installation"
    echo ""
    read -p "Enter your choice (1-4): " choice
    
    case $choice in
        1)
            echo ""
            echo "Downloading llama3.2... This may take a few minutes."
            ollama pull llama3.2
            ;;
        2)
            echo ""
            echo "Downloading mistral... This may take a few minutes."
            ollama pull mistral
            ;;
        3)
            echo ""
            echo "Downloading phi3... This may take a few minutes."
            ollama pull phi3
            ;;
        *)
            echo ""
            echo "Skipping model installation."
            echo "You can install a model later with: ollama pull llama3.2"
            echo ""
            exit 0
            ;;
    esac
fi

# Test AI
echo ""
echo "============================================"
echo "  Testing AI Integration"
echo "============================================"
echo ""
echo "Sending test prompt to AI..."
echo ""

echo "You are a financial advisor. Analyze this: Food expenses \$500/month (40% of budget). Provide one actionable tip." | ollama run llama3.2

echo ""
echo "============================================"
echo "  Setup Complete!"
echo "============================================"
echo ""
echo -e "${GREEN}AI is ready to use!${NC} You can now:"
echo "  1. Start the Financial Planner application"
echo "  2. The AI will automatically provide personalized recommendations"
echo ""
echo "Available models:"
ollama list
echo ""
echo "To keep Ollama running, use: ollama serve"
echo ""

