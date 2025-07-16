# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1] --2025-07-15
### Added
- Unescape Feature: New functionality to convert escape sequences and HTML entities in SQL strings
- Web:
  - Added "Unescape" button with light green styling
  - Auto-save functionality for editor content and settings
  - Persistent state across page refreshes
- IDEA Plugin:
  - Added "Unescape" button to plugin interface

### Changed
- Web:
  - Update the panel high to improved user experience 
- IDEA Plugin:
  - Update the error message panel
  - Update the layout to improved user experience

### Fixed
- Fix html lost css when click the format button
- Fix miss spaces after the CTE when sql mode is Minify

## [1.0]
### Added
- Initial release of StarSQLs SQL formatter
- Support for StarRocks SQL syntax formatting
- Rich configuration options for SQL formatting
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
