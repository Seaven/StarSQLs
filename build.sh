#!/bin/bash

# StarSQLs Build Script
# Version: 1.0

set -e

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$PROJECT_ROOT/output"
BIN_DIR="$OUTPUT_DIR/bin"
LIB_DIR="$OUTPUT_DIR/lib"
LOG_DIR="$OUTPUT_DIR/log"

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

# Function to check if Maven is available
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    print_status "Maven version: $(mvn -version | head -n 1)"
}

# Function to create output directory structure
create_output_structure() {
    print_status "Creating output directory structure..."
    
    # Create main output directory
    mkdir -p "$OUTPUT_DIR"
    
    # Create subdirectories
    mkdir -p "$BIN_DIR"
    mkdir -p "$LIB_DIR"
    mkdir -p "$LOG_DIR"
    
    print_status "Created output directory: $OUTPUT_DIR"
    print_status "Created bin directory: $BIN_DIR"
    print_status "Created lib directory: $LIB_DIR"
    print_status "Created log directory: $LOG_DIR"
}

# Function to build the project
build_project() {
    print_status "Building StarSQLs project..."
    
    cd "$PROJECT_ROOT"
    
    # Clean and compile
    mvn clean compile
    
    # Package the project
    mvn package -DskipTests
    
    print_status "Project build completed successfully!"
}

# Function to get project version from pom.xml
get_project_version() {
    cd "$PROJECT_ROOT"
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

# Function to copy artifacts to output directory
copy_artifacts() {
    print_status "Copying artifacts to output directory..."
    
    # Get project version
    PROJECT_VERSION=$(get_project_version)
    print_status "Project version: $PROJECT_VERSION"
    
    # Copy web JAR to lib directory
    WEB_JAR="starsqls-web-$PROJECT_VERSION.jar"
    if [ -f "$PROJECT_ROOT/web/target/$WEB_JAR" ]; then
        cp "$PROJECT_ROOT/web/target/$WEB_JAR" "$LIB_DIR/"
        print_status "Copied $WEB_JAR to $LIB_DIR"
    else
        print_warning "$WEB_JAR not found in web/target/"
    fi
    
    # Copy core JAR to lib directory
    CORE_JAR="starsqls-core-$PROJECT_VERSION.jar"
    if [ -f "$PROJECT_ROOT/core/target/$CORE_JAR" ]; then
        cp "$PROJECT_ROOT/core/target/$CORE_JAR" "$LIB_DIR/"
        print_status "Copied $CORE_JAR to $LIB_DIR"
    else
        print_warning "$CORE_JAR not found in core/target/"
    fi
    
    # Note: No need to copy dependencies for web module since it's a Spring Boot fat jar
    # The web JAR already contains all necessary dependencies
    print_status "Skipping dependency copy for web module (fat jar contains all dependencies)"
    
    print_status "Artifacts copied successfully!"
}

# Function to copy scripts to bin directory
copy_scripts() {
    print_status "Copying scripts to bin directory..."
    
    # Copy web scripts
    if [ -d "$PROJECT_ROOT/web/scripts" ]; then
        cp "$PROJECT_ROOT/web/scripts/"*.sh "$BIN_DIR/"
        print_status "Copied web scripts to $BIN_DIR"
        
        # Update version in copied scripts if needed
        for script in "$BIN_DIR"/*.sh; do
            if [ -f "$script" ]; then
                # Make sure the script has execute permission
                chmod +x "$script"
            fi
        done
    fi
    
    print_status "Scripts copied successfully!"
}

# Main execution
main() {
    print_status "StarSQLs Build Script"
    print_status "====================="
    
    check_maven
    create_output_structure
    build_project
    copy_artifacts
    copy_scripts
    
    print_status "Build completed successfully!"
    print_status "Output directory: $OUTPUT_DIR"
}

# Run main function
main "$@" 