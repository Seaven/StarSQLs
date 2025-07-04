#!/bin/bash

# StarSQLs Web Service Restart Script
# Version: 1.0

set -e

# Configuration
APP_NAME="starsqls-web"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
START_SCRIPT="$SCRIPT_DIR/start.sh"
STOP_SCRIPT="$SCRIPT_DIR/stop.sh"

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

# Function to restart the application
restart_application() {
    print_status "StarSQLs Web Service Restart Script"
    print_status "===================================="
    
    # Stop the application
    print_status "Stopping application..."
    if ! "$STOP_SCRIPT"; then
        print_warning "Stop script failed, but continuing with restart..."
    fi
    
    # Wait a moment for cleanup
    sleep 2
    
    # Start the application
    print_status "Starting application..."
    if ! "$START_SCRIPT"; then
        print_error "Failed to start application"
        exit 1
    fi
    
    print_status "Restart completed successfully!"
}

# Main execution
main() {
    restart_application
}

# Run main function
main "$@" 