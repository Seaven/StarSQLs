# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0] - 2025-07-04

### Added
- Initial release of StarSQLs SQL formatter
- Support for StarRocks SQL syntax formatting
- Rich configuration options for SQL formatting
- Command line interface
- Web interface with REST API
- IntelliJ IDEA plugin
- Comprehensive formatting options:
  - Indentation settings
  - Maximum line length control
  - Keyword case style (UPPER/LOWER/ORIGINAL)
  - Comma position style (END/START)
  - Function and expression parameter formatting
  - CTE, JOIN, SELECT, GROUP BY, ORDER BY clause formatting
  - Subquery formatting support

### Technical Details
- Built with Java 17
- Uses ANTLR4 for SQL parsing
- Spring Boot 2.7.18 for web interface
- IntelliJ Platform 2024.3 compatibility
- Maven and Gradle build support

### Supported Features
- SQL syntax parsing and validation
- Customizable formatting rules
- Multiple output formats (pretty print, minified)
- Plugin integration with IntelliJ IDEA
- RESTful API for web integration 