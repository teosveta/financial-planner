@echo off
REM Financial Planner - Claude AI Integration Test Script
REM Tests real AI functionality with Claude API

echo.
echo ========================================
echo  Financial Planner - Claude AI Test
echo ========================================
echo.

set BASE_URL=http://localhost:8081/api/v1/transactions

REM Colors (Windows 10+)
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set CYAN=[96m
set NC=[0m

echo %CYAN%Step 1: Health Check%NC%
echo ---------------------
curl -s %BASE_URL%/health
echo.
echo.

echo %CYAN%Step 2: Check Claude AI Status%NC%
echo -----------------------------
curl -s %BASE_URL%/ai/status
echo.
echo.

echo %CYAN%Step 3: Test Claude AI with Simple Prompt%NC%
echo --------------------------------------
curl -s -X POST %BASE_URL%/ai/test ^
  -H "Content-Type: application/json" ^
  -d "{\"prompt\": \"Give me one financial tip in 30 words.\"}"
echo.
echo.

echo %CYAN%Step 4: Create Test Transactions (AI Auto-Categorization)%NC%
echo ------------------------------------------------------

echo Creating transaction 1: Whole Foods (Food)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"merchantName\":\"Whole Foods Market\",\"description\":\"Weekly groceries\",\"amount\":127.50}"
echo.

echo Creating transaction 2: Uber (Transport)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"merchantName\":\"Uber\",\"description\":\"Ride to airport\",\"amount\":45.00}"
echo.

echo Creating transaction 3: Netflix (Entertainment)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"merchantName\":\"Netflix\",\"description\":\"Monthly subscription\",\"amount\":15.99}"
echo.

echo Creating transaction 4: ConEd (Bills)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"merchantName\":\"ConEdison\",\"description\":\"Electricity bill\",\"amount\":125.00}"
echo.

echo Creating transaction 5: Amazon (Shopping)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"merchantName\":\"Amazon\",\"description\":\"Wireless headphones\",\"amount\":89.99}"
echo.

echo Creating transaction 6: Planet Fitness (Health)
curl -s -X POST %BASE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"merchantName\":\"Planet Fitness\",\"description\":\"Monthly gym membership\",\"amount\":24.99}"
echo.
echo.

echo %CYAN%Step 5: Get All Transactions (Verify AI Categories)%NC%
echo -----------------------------------------------
curl -s %BASE_URL% | jq "."
echo.
echo.

echo %CYAN%Step 6: Get AI-Powered Analysis Report%NC%
echo ------------------------------------
curl -s "%BASE_URL%/analysis?period=monthly" | jq "."
echo.
echo.

echo %CYAN%Step 7: Category Statistics%NC%
echo ------------------------
curl -s "%BASE_URL%/stats/FOOD?period=monthly" | jq "."
echo.
echo.

echo ========================================
echo  Test Complete!
echo ========================================
echo.
echo %GREEN%✓ All tests executed%NC%
echo.
echo %YELLOW%What to verify:%NC%
echo 1. AI Status shows "Claude AI" and "available: true"
echo 2. Transactions have intelligent categories (not all "OTHER")
echo 3. Analysis report has personalized AI recommendations
echo 4. Recommendations mention specific dollar amounts and actions
echo.
echo %CYAN%Next steps:%NC%
echo - Review the AI recommendations in the analysis report
echo - Check transaction categories are accurate
echo - Try the frontend to see AI insights in action
echo.
pause

