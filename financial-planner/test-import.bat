@echo off
REM ====================================================================
REM Test JSON Import Functionality
REM This script tests the JSON import feature with example files
REM ====================================================================

echo.
echo ============================================
echo   Testing JSON Import Functionality
echo ============================================
echo.

REM Check if server is running
curl -s http://localhost:8080/api/v1/import/health >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Financial Planner is not running!
    echo.
    echo Please start the application first:
    echo   mvnw.cmd spring-boot:run
    echo.
    pause
    exit /b 1
)

echo [OK] Server is running
echo.

REM Test 1: Import example-simple.json
echo ============================================
echo Test 1: Import Simple Format
echo ============================================
echo File: example-simple.json
echo.

curl -X POST http://localhost:8080/api/v1/import/file ^
  -F "file=@example-simple.json" ^
  -H "Accept: application/json"

echo.
echo.
timeout /t 2 /nobreak >nul

REM Test 2: Import example-import.json
echo ============================================
echo Test 2: Import Full Format
echo ============================================
echo File: example-import.json
echo.

curl -X POST http://localhost:8080/api/v1/import/file ^
  -F "file=@example-import.json" ^
  -H "Accept: application/json"

echo.
echo.
timeout /t 2 /nobreak >nul

REM Test 3: Get example format
echo ============================================
echo Test 3: Get Example Format
echo ============================================
echo.

curl http://localhost:8080/api/v1/import/example

echo.
echo.
timeout /t 2 /nobreak >nul

REM Test 4: Check analysis after import
echo ============================================
echo Test 4: Check Analysis After Import
echo ============================================
echo.

curl http://localhost:8080/api/v1/transactions/analysis?period=monthly

echo.
echo.

echo ============================================
echo   All Tests Completed!
echo ============================================
echo.
echo Check the dashboard: http://localhost:8080
echo.
echo You should see:
echo   - Imported transactions
echo   - Automatic categorization
echo   - AI analysis and recommendations
echo.

pause

