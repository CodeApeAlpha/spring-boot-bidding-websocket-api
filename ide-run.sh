#!/bin/bash

# Lean Bidding Application Runner
# WebSocket-focused live bidding system

echo "=== Lean Bidding Application Runner ==="

# Check if any Java application is already running
if pgrep -f "BiddingWebSocketApplication" > /dev/null; then
    echo "⚠️  BiddingWebSocketApplication is already running!"
    echo "Please stop the existing instance first."
    echo "Run: pkill -f BiddingWebSocketApplication"
    exit 1
fi

# Function to start MySQL
start_mysql() {
    echo "🐬 Starting MySQL..."
    docker run -d --name bidding-mysql \
        -e MYSQL_ROOT_PASSWORD=password \
        -e MYSQL_DATABASE=bidding_db \
        -p 3306:3306 \
        mysql:8.0 2>/dev/null || echo "MySQL container already running"
    
    echo "⏳ Waiting for MySQL to be ready..."
    sleep 10
    echo "✅ MySQL is ready!"
}

# Function to stop services
stop_services() {
    echo "🛑 Stopping services..."
    docker stop bidding-mysql 2>/dev/null
    docker rm bidding-mysql 2>/dev/null
    echo "✅ Services stopped!"
}

# Function to run application
run_app() {
    echo "🚀 Starting lean bidding application..."
    export PATH="$PWD/apache-maven-3.9.11/bin:$PATH"
    mvn spring-boot:run
}

# Main script logic
case "$1" in
    "mysql")
        start_mysql
        ;;
    "run")
        start_mysql
        run_app
        ;;
    "stop")
        stop_services
        ;;
    *)
        echo "Usage: $0 {mysql|run|stop}"
        echo "  mysql - Start MySQL only"
        echo "  run   - Start MySQL and run application"
        echo "  stop  - Stop all services"
        exit 1
        ;;
esac
