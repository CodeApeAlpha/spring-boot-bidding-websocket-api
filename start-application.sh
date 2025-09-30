#!/bin/bash

# Centralized Live Bidding Application Starter
# This script provides a simple way to start the infrastructure

echo "🚀 Starting Centralized Live Bidding Application"
echo "=============================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Use the improved ide-run.sh script for better service management
echo "📦 Starting infrastructure using improved service manager..."

# Check if ide-run.sh exists and is executable
if [ -f "./ide-run.sh" ] && [ -x "./ide-run.sh" ]; then
    echo "✅ Using improved service manager (ide-run.sh)"
    echo ""
    echo "Available commands:"
    echo "  ./ide-run.sh status    - Check service status"
    echo "  ./ide-run.sh mysql     - Start MySQL only"
    echo "  ./ide-run.sh run       - Start MySQL and run application"
    echo "  ./ide-run.sh stop      - Stop all services"
    echo "  ./ide-run.sh cleanup   - Clean up everything"
    echo ""
    echo "🎯 Recommended: Use './ide-run.sh run' for full startup"
    echo "   Or use your IDE's Run button after starting MySQL"
    echo ""
    
    # Start MySQL using the improved script
    ./ide-run.sh mysql
    
    echo ""
    echo "✅ Infrastructure ready!"
    echo "   - MySQL: localhost:3306"
    echo ""
    echo "🌐 Application will be available at:"
    echo "   - Main App: http://localhost:8080"
    echo "   - API Docs: http://localhost:8080/swagger-ui.html"
    echo "   - WebSocket: ws://localhost:8080/ws"
    echo ""
    echo "🎯 Next steps:"
    echo "   1. Use your IDE's Run button to start the application"
    echo "   2. Or run: ./ide-run.sh run"
    echo "   3. Or run: mvn spring-boot:run"
    echo ""
    echo "🛑 To stop: ./ide-run.sh stop"
    
else
    echo "⚠️  ide-run.sh not found or not executable"
    echo "   Falling back to basic Docker Compose startup..."
    
    # Fallback to basic startup
    echo "📦 Starting MySQL container..."
    docker run -d --name bidding-mysql \
        -e MYSQL_ROOT_PASSWORD=password \
        -e MYSQL_DATABASE=bidding_db \
        -e MYSQL_USER=bidding_user \
        -e MYSQL_PASSWORD=bidding_pass \
        -p 3306:3306 \
        mysql:8.0
    
    echo "⏳ Waiting for MySQL to be ready..."
    sleep 15
    
    echo "✅ Infrastructure ready!"
    echo "   - MySQL: localhost:3306"
    echo ""
    echo "🎯 Now you can run the application using:"
    echo "   - IDE Run button"
    echo "   - Or: mvn spring-boot:run"
    echo ""
    echo "🌐 Application will be available at:"
    echo "   - Main App: http://localhost:8080"
    echo "   - API Docs: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "🛑 To stop: docker stop bidding-mysql && docker rm bidding-mysql"
fi
