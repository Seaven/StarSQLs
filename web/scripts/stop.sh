#!/bin/bash

# StarSQLs Web Service Stop Script
# Version: 1.0

set -e

# Configuration
APP_NAME="starsqls-web"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../"
PID_FILE="$OUTPUT_DIR/$APP_NAME.pid"

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

# Function to stop the application
stop_application() {
    if [ ! -f "$PID_FILE" ]; then
        print_warning "PID file not found at: $PID_FILE"
        print_warning "Application may not be running."
        return 0
    fi
    
    PID=$(cat "$PID_FILE")
    
    if ! ps -p "$PID" > /dev/null 2>&1; then
        print_warning "Process with PID $PID is not running. Removing stale PID file."
        rm -f "$PID_FILE"
        return 0
    fi
    
    print_status "Stopping $APP_NAME (PID: $PID)..."
    
    # Try graceful shutdown first
    if ! kill "$PID"; then
        print_error "Failed to send TERM signal to process $PID"
        return 1
    fi
    
    # Wait for graceful shutdown (up to 30 seconds)
    for i in {1..30}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            print_status "Application stopped gracefully"
            rm -f "$PID_FILE"
            return 0
        fi
        sleep 1
    done
    
    # Force kill if graceful shutdown failed
    print_warning "Graceful shutdown failed. Force killing process..."
    if ! kill -9 "$PID"; then
        print_error "Failed to send KILL signal to process $PID"
        return 1
    fi
    
    # Wait a moment and check
    sleep 2
    if ! ps -p "$PID" > /dev/null 2>&1; then
        print_status "Application force stopped"
        rm -f "$PID_FILE"
        return 0
    else
        print_error "Failed to stop application - process still running after force kill"
        return 1
    fi
}

# Function to check if application is running
check_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

# Function to print configuration
print_configuration() {
    print_status "Configuration:"
    print_status "  Output Directory: $OUTPUT_DIR"
    print_status "  PID File: $PID_FILE"
    echo
}

# Main execution
main() {
    print_status "StarSQLs Web Service Stop Script"
    print_status "=================================="
    
    print_configuration
    
    if ! check_running; then
        print_warning "Application is not running"
        exit 0
    fi
    
    if ! stop_application; then
        print_error "Failed to stop application"
        exit 1
    fi
    
    print_status "Stop operation completed successfully!"
    exit 0
}

# Run main function
main "$@" 
