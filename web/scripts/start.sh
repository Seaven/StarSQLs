#!/bin/bash

# StarSQLs Web Service Startup Script
# Version: 1.0

set -e

# Configuration
APP_NAME="starsqls-web"
APP_JAR="starsqls-web-1.0.jar"
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR_PATH="$APP_DIR/target/$APP_JAR"
PID_FILE="$APP_DIR/logs/$APP_NAME.pid"
LOG_DIR="$APP_DIR/logs"
LOG_FILE="$LOG_DIR/$APP_NAME.log"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"
SPRING_PROFILES="prod"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Java is available
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt "17" ]; then
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    print_status "Java version check passed: $(java -version 2>&1 | head -n 1)"
}

# Function to create necessary directories
create_directories() {
    mkdir -p "$LOG_DIR"
    print_status "Created log directory: $LOG_DIR"
}

# Function to check if application is already running
check_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            print_warning "Application is already running with PID: $PID"
            return 0
        else
            print_warning "PID file exists but process is not running. Removing stale PID file."
            rm -f "$PID_FILE"
        fi
    fi
    return 1
}

# Function to start the application
start_application() {
    print_status "Starting $APP_NAME..."
    
    # Check if JAR file exists
    if [ ! -f "$JAR_PATH" ]; then
        print_error "JAR file not found: $JAR_PATH"
        print_error "Please build the project first: mvn clean package"
        exit 1
    fi
    
    # Start the application
    nohup java $JAVA_OPTS \
        -Dspring.profiles.active=$SPRING_PROFILES \
        -Dserver.port=8080 \
        -jar "$JAR_PATH" \
        > "$LOG_FILE" 2>&1 &
    
    # Save PID
    echo $! > "$PID_FILE"
    
    # Wait a moment for the application to start
    sleep 3
    
    # Check if application started successfully
    if ps -p $(cat "$PID_FILE") > /dev/null 2>&1; then
        print_status "Application started successfully with PID: $(cat "$PID_FILE")"
        print_status "Log file: $LOG_FILE"
        print_status "Application URL: http://localhost:8080"
    else
        print_error "Failed to start application. Check logs: $LOG_FILE"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# Main execution
main() {
    print_status "StarSQLs Web Service Startup Script"
    print_status "====================================="
    
    check_java
    create_directories
    
    if check_running; then
        print_warning "Application is already running. Use stop.sh to stop it first."
        exit 0
    fi
    
    start_application
    
    print_status "Startup completed successfully!"
}

# Run main function
main "$@" 