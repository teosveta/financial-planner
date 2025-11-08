@echo off
echo 🚀 Starting Financial Planner...
echo.
echo Building the application...
call mvnw.cmd clean install

echo.
echo Starting Spring Boot application...
echo The app will be available at: http://localhost:8080
echo.

call mvnw.cmd spring-boot:run
