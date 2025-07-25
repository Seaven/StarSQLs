#!/bin/bash

# StarSQLs Web Service Startup Script
# Version: 1.0

set -e

# Configuration
APP_NAME="starsqls-web"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../"
PID_FILE="$OUTPUT_DIR/$APP_NAME.pid"
LOG_DIR="$OUTPUT_DIR/log"
LOG_FILE="$LOG_DIR/$APP_NAME.log"
JAR_PATH="$OUTPUT_DIR/lib/$APP_NAME.jar"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"
SPRING_PROFILES="prod"
SERVER_PORT="8080"

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

# Function to find JAR file with version
find_jar_file() {
    local lib_dir="$OUTPUT_DIR/lib"
    local jar_pattern="$APP_NAME-*.jar"
    
    if [ ! -d "$lib_dir" ]; then
        print_error "Lib directory not found: $lib_dir"
        return 1
    fi
    
    # Find the most recent JAR file matching the pattern
    local jar_file=$(find "$lib_dir" -name "$jar_pattern" -type f | sort -V | tail -n 1)
    
    if [ -z "$jar_file" ]; then
        print_error "No JAR file found matching pattern: $jar_pattern"
        print_error "Expected location: $lib_dir/$jar_pattern"
        return 1
    fi
    
    echo "$jar_file"
}

# Function to extract version from JAR filename
extract_version() {
    local jar_file="$1"
    local filename=$(basename "$jar_file")
    local version=$(echo "$filename" | sed "s/$APP_NAME-\(.*\)\.jar/\1/")
    echo "$version"
}

# Function to check if Java is available
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        return 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt "17" ]; then
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        return 1
    fi
    
    print_status "Java version check passed: $(java -version 2>&1 | head -n 1)"
    return 0
}

# Function to create necessary directories
create_directories() {
    if ! mkdir -p "$LOG_DIR"; then
        print_error "Failed to create log directory: $LOG_DIR"
        return 1
    fi
    
    if ! mkdir -p "$(dirname "$PID_FILE")"; then
        print_error "Failed to create PID file directory: $(dirname "$PID_FILE")"
        return 1
    fi
    
    print_status "Created directories: $LOG_DIR, $(dirname "$PID_FILE")"
    return 0
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

# Function to check if service is running
check_service_running() {
    local pid="$1"
    
    # Check if process is still running
    if ! ps -p "$pid" > /dev/null 2>&1; then
        print_error "Process is no longer running"
        return 1
    fi
    
    # Simple port check
    if command -v netstat >/dev/null 2>&1 && netstat -tuln 2>/dev/null | grep -q ":$SERVER_PORT "; then
        print_status "Service is running and port is listening"
        return 0
    fi
    
    if command -v ss >/dev/null 2>&1 && ss -tuln 2>/dev/null | grep -q ":$SERVER_PORT "; then
        print_status "Service is running and port is listening"
        return 0
    fi
    
    # If port check fails, just check if process is running
    print_status "Service process is running (port check unavailable)"
    return 0
}

# Function to start the application
start_application() {
    print_status "Starting $APP_NAME..."
    
    # Find JAR file with version
    JAR_PATH=$(find_jar_file)
    if [ $? -ne 0 ]; then
        return 1
    fi
    
    # Extract version from JAR filename
    VERSION=$(extract_version "$JAR_PATH")
    
    print_status "Found JAR file: $JAR_PATH (Version: $VERSION)"
    
    # Start the application
    nohup java $JAVA_OPTS \
        -Dspring.profiles.active=$SPRING_PROFILES \
        -Dserver.port=$SERVER_PORT \
        -jar "$JAR_PATH" \
        > "$LOG_FILE" 2>&1 &
    
    if [ $? -ne 0 ]; then
        print_error "Failed to start Java application"
        return 1
    fi
    
    # Save PID
    local app_pid=$!
    echo $app_pid > "$PID_FILE"
    
    print_status "Application started with PID: $app_pid"
    print_status "Log file: $LOG_FILE"
    print_status "Application URL: http://localhost:$SERVER_PORT"
    
    # Wait a moment for the application to start
    sleep 3
    
    # Check if service is running
    if ! check_service_running "$app_pid"; then
        print_error "Service check failed"
        # Clean up
        kill -TERM "$app_pid" 2>/dev/null || true
        rm -f "$PID_FILE"
        return 1
    fi
    
    print_status "Application started successfully"
    return 0
}

# Function to print configuration
print_configuration() {
    print_status "Configuration:"
    print_status "  Output Directory: $OUTPUT_DIR"
    print_status "  PID File: $PID_FILE"
    print_status "  Log Directory: $LOG_DIR"
    print_status "  Log File: $LOG_FILE"
    print_status "  JAR Path: $JAR_PATH"
    print_status "  Java Options: $JAVA_OPTS"
    print_status "  Spring Profiles: $SPRING_PROFILES"
    print_status "  Server Port: $SERVER_PORT"
    echo
}

# Main execution
main() {
    print_status "StarSQLs Web Service Startup Script"
    print_status "====================================="
    
    # Check Java
    if ! check_java; then
        print_error "Java check failed"
        exit 1
    fi
    
    # Create directories
    if ! create_directories; then
        print_error "Failed to create directories"
        exit 1
    fi
    
    # Check if already running
    if check_running; then
        print_warning "Application is already running. Use stop.sh to stop it first."
        exit 0
    fi
    
    # Start application
    if ! start_application; then
        print_error "Failed to start application"
        exit 1
    fi
    
    print_status "Startup completed successfully!"
    exit 0
}

# Run main function
main "$@" 
