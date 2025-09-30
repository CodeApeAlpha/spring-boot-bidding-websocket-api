#!/bin/bash

echo "🚀 Starting Centralized Live Bidding Application"
echo "=============================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start Redis and MySQL
echo "📦 Starting Redis and MySQL containers..."
docker-compose -f docker-compose-redis.yml up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are running
if ! docker ps | grep -q "bidding-redis\|bidding-mysql"; then
    echo "❌ Failed to start Redis or MySQL containers"
    exit 1
fi

echo "✅ Infrastructure ready!"
echo "   - Redis: localhost:6379"
echo "   - MySQL: localhost:3306"
echo ""
echo "🎯 Now you can run the application using:"
echo "   - IDE Run button (after this script completes)"
echo "   - Or: mvn spring-boot:run"
echo ""
echo "🌐 Application will be available at:"
echo "   - Main App: http://localhost:8080"
echo "   - API Docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop infrastructure when done"
