@echo off
REM ====================================================================
REM Ollama AI Setup Script for Windows
REM This script helps you install and configure Ollama for AI features
REM ====================================================================

echo.
echo ============================================
echo   Financial Planner - AI Setup (Ollama)
echo ============================================
echo.

REM Check if Ollama is already installed
where ollama >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Ollama is already installed!
    echo.
    goto :check_service
) else (
    echo [!] Ollama is NOT installed.
    echo.
    echo Please install Ollama from: https://ollama.com/download
    echo.
    echo After installation:
    echo   1. Close this window
    echo   2. Run this script again
    echo.
    pause
    start https://ollama.com/download
    exit /b 1
)

:check_service
echo Checking if Ollama service is running...
timeout /t 2 /nobreak >nul

curl -s http://localhost:11434/api/tags >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Ollama service is running!
    echo.
    goto :check_model
) else (
    echo [!] Ollama service is NOT running.
    echo.
    echo Starting Ollama service...
    start "Ollama Server" cmd /c "ollama serve"
    echo Waiting for service to start...
    timeout /t 5 /nobreak >nul
    echo.
)

:check_model
echo Checking for AI models...
echo.

ollama list 2>nul | findstr "llama3.2" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Model 'llama3.2' is already installed!
    echo.
    goto :test_ai
) else (
    echo [!] Model 'llama3.2' is NOT installed.
    echo.
    echo Choose an AI model to download:
    echo   1. llama3.2 (Recommended, ~2GB)
    echo   2. mistral (High quality, ~4GB)
    echo   3. phi3 (Fastest, ~2.3GB)
    echo   4. Skip model installation
    echo.
    set /p choice="Enter your choice (1-4): "
    
    if "%choice%"=="1" (
        echo.
        echo Downloading llama3.2... This may take a few minutes.
        ollama pull llama3.2
    ) else if "%choice%"=="2" (
        echo.
        echo Downloading mistral... This may take a few minutes.
        ollama pull mistral
    ) else if "%choice%"=="3" (
        echo.
        echo Downloading phi3... This may take a few minutes.
        ollama pull phi3
    ) else (
        echo.
        echo Skipping model installation.
        echo You can install a model later with: ollama pull llama3.2
        echo.
        goto :finish
    )
)

:test_ai
echo.
echo ============================================
echo   Testing AI Integration
echo ============================================
echo.
echo Sending test prompt to AI...
echo.

echo You are a financial advisor. Analyze this: Food expenses $500/month (40%% of budget). Provide one actionable tip. | ollama run llama3.2

echo.
echo ============================================
echo   Setup Complete!
echo ============================================
echo.
echo AI is ready to use! You can now:
echo   1. Start the Financial Planner application
echo   2. The AI will automatically provide personalized recommendations
echo.
echo Available models:
ollama list
echo.

:finish
echo Press any key to exit...
pause >nul

