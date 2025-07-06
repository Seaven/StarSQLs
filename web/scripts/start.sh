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
    mkdir -p "$(dirname "$PID_FILE")"
    print_status "Created directories: $LOG_DIR, $(dirname "$PID_FILE")"
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
    
    # Find JAR file with version
    JAR_PATH=$(find_jar_file)
    if [ $? -ne 0 ]; then
        exit 1
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
    
    # Save PID
    echo $! > "$PID_FILE"
    
    # Wait a moment for the application to start
    sleep 3
    
    # Check if application started successfully
    if ps -p $(cat "$PID_FILE") > /dev/null 2>&1; then
        print_status "Application started successfully with PID: $(cat "$PID_FILE")"
        print_status "Log file: $LOG_FILE"
        print_status "Application URL: http://localhost:$SERVER_PORT"
    else
        print_error "Failed to start application. Check logs: $LOG_FILE"
        rm -f "$PID_FILE"
        exit 1
    fi
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
