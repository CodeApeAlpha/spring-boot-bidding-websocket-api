#!/bin/bash

# Lean Bidding Application Runner
# WebSocket-focused live bidding system with proper service management

set -e  # Exit on any error

echo "=== Lean Bidding Application Runner ==="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
MYSQL_CONTAINER="bidding-mysql"
MYSQL_PORT="3306"
APP_PORT="8080"
MAVEN_PATH="$PWD/apache-maven-3.9.11/bin"

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if a port is in use
is_port_in_use() {
    local port=$1
    lsof -i :$port >/dev/null 2>&1
}

# Function to check if MySQL container is running
is_mysql_running() {
    docker ps --format "table {{.Names}}" | grep -q "^${MYSQL_CONTAINER}$"
}

# Function to check if MySQL is healthy
is_mysql_healthy() {
    if is_mysql_running; then
        docker exec $MYSQL_CONTAINER mysqladmin ping -h localhost -u root -ppassword >/dev/null 2>&1
    else
        return 1
    fi
}

# Function to check if application is running
is_app_running() {
    pgrep -f "BiddingWebSocketApplication" >/dev/null 2>&1
}

# Function to wait for MySQL to be ready
wait_for_mysql() {
    local max_attempts=30
    local attempt=1
    
    print_status $YELLOW "⏳ Waiting for MySQL to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if is_mysql_healthy; then
            print_status $GREEN "✅ MySQL is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    print_status $RED "❌ MySQL failed to start within expected time"
    return 1
}

# Function to start MySQL
start_mysql() {
    print_status $BLUE "🐬 Checking MySQL status..."
    
    if is_mysql_running; then
        if is_mysql_healthy; then
            print_status $GREEN "✅ MySQL is already running and healthy"
            return 0
        else
            print_status $YELLOW "⚠️  MySQL container is running but not healthy, restarting..."
            docker stop $MYSQL_CONTAINER >/dev/null 2>&1 || true
            docker rm $MYSQL_CONTAINER >/dev/null 2>&1 || true
        fi
    fi
    
    # Check if port is already in use by another process
    if is_port_in_use $MYSQL_PORT; then
        print_status $RED "❌ Port $MYSQL_PORT is already in use by another process"
        print_status $YELLOW "Please free up port $MYSQL_PORT or stop the conflicting service"
        return 1
    fi
    
    print_status $BLUE "🐬 Starting MySQL container..."
    docker run -d --name $MYSQL_CONTAINER \
        -e MYSQL_ROOT_PASSWORD=password \
        -e MYSQL_DATABASE=bidding_db \
        -e MYSQL_USER=bidding_user \
        -e MYSQL_PASSWORD=bidding_pass \
        -p $MYSQL_PORT:3306 \
        --health-cmd="mysqladmin ping -h localhost -u root -ppassword" \
        --health-interval=5s \
        --health-timeout=3s \
        --health-retries=3 \
        mysql:8.0 >/dev/null 2>&1
    
    wait_for_mysql
}

# Function to stop services gracefully
stop_services() {
    print_status $BLUE "🛑 Stopping services gracefully..."
    
    # Stop Java application first
    if is_app_running; then
        print_status $YELLOW "🛑 Stopping Spring Boot application..."
        pkill -TERM -f "BiddingWebSocketApplication" || true
        
        # Wait for graceful shutdown
        local count=0
        while is_app_running && [ $count -lt 10 ]; do
            echo -n "."
            sleep 1
            ((count++))
        done
        
        if is_app_running; then
            print_status $YELLOW "⚠️  Application didn't stop gracefully, forcing shutdown..."
            pkill -KILL -f "BiddingWebSocketApplication" || true
        fi
        
        print_status $GREEN "✅ Application stopped"
    else
        print_status $BLUE "ℹ️  No application running"
    fi
    
    # Stop MySQL container
    if is_mysql_running; then
        print_status $YELLOW "🛑 Stopping MySQL container..."
        docker stop $MYSQL_CONTAINER >/dev/null 2>&1 || true
        docker rm $MYSQL_CONTAINER >/dev/null 2>&1 || true
        print_status $GREEN "✅ MySQL stopped"
    else
        print_status $BLUE "ℹ️  MySQL container not running"
    fi
    
    print_status $GREEN "✅ All services stopped!"
}

# Function to run application
run_app() {
    print_status $BLUE "🚀 Starting lean bidding application..."
    
    # Check if application is already running
    if is_app_running; then
        print_status $YELLOW "⚠️  Application is already running!"
        print_status $BLUE "Use '$0 stop' to stop the existing instance first"
        return 1
    fi
    
    # Check if port is available
    if is_port_in_use $APP_PORT; then
        print_status $RED "❌ Port $APP_PORT is already in use"
        print_status $YELLOW "Please free up port $APP_PORT or stop the conflicting service"
        return 1
    fi
    
    # Set up Maven path
    export PATH="$MAVEN_PATH:$PATH"
    
    # Start the application
    print_status $GREEN "🚀 Launching Spring Boot application..."
    mvn spring-boot:run
}

# Function to show status
show_status() {
    print_status $BLUE "📊 Service Status:"
    echo
    
    # Check MySQL
    if is_mysql_running; then
        if is_mysql_healthy; then
            print_status $GREEN "✅ MySQL: Running and healthy"
        else
            print_status $YELLOW "⚠️  MySQL: Running but not healthy"
        fi
    else
        print_status $RED "❌ MySQL: Not running"
    fi
    
    # Check Application
    if is_app_running; then
        print_status $GREEN "✅ Application: Running"
    else
        print_status $RED "❌ Application: Not running"
    fi
    
    # Check Ports
    if is_port_in_use $MYSQL_PORT; then
        print_status $GREEN "✅ Port $MYSQL_PORT: In use"
    else
        print_status $YELLOW "⚠️  Port $MYSQL_PORT: Available"
    fi
    
    if is_port_in_use $APP_PORT; then
        print_status $GREEN "✅ Port $APP_PORT: In use"
    else
        print_status $YELLOW "⚠️  Port $APP_PORT: Available"
    fi
}

# Function to clean up everything
cleanup() {
    print_status $BLUE "🧹 Cleaning up all resources..."
    stop_services
    
    # Remove any orphaned containers
    docker container prune -f >/dev/null 2>&1 || true
    
    print_status $GREEN "✅ Cleanup complete!"
}

# Note: Cleanup is handled manually via the cleanup command

# Main script logic
case "$1" in
    "mysql")
        start_mysql
        ;;
    "run")
        start_mysql && run_app
        ;;
    "stop")
        stop_services
        ;;
    "status")
        show_status
        ;;
    "cleanup")
        cleanup
        ;;
    "restart")
        stop_services
        sleep 2
        start_mysql && run_app
        ;;
    *)
        echo "Usage: $0 {mysql|run|stop|status|cleanup|restart}"
        echo
        echo "Commands:"
        echo "  mysql    - Start MySQL only"
        echo "  run      - Start MySQL and run application"
        echo "  stop     - Stop all services gracefully"
        echo "  status   - Show current service status"
        echo "  cleanup  - Clean up all resources"
        echo "  restart  - Stop and restart all services"
        echo
        echo "Examples:"
        echo "  $0 status    # Check what's running"
        echo "  $0 run       # Start everything"
        echo "  $0 stop      # Stop everything gracefully"
        echo "  $0 restart   # Restart everything"
        exit 1
        ;;
esac
