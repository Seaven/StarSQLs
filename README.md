# StarSQLs - StarRocks SQL Formatter

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Version](https://img.shields.io/badge/Version-1.0-green.svg)](CHANGELOG.md)

StarSQLs is a comprehensive tool suite developed for StarRocks SQL, currently providing SQL formatting capabilities. Compared to other formatting tools, it offers richer formatting options for various SQL clauses, supporting command line, web interface, and IntelliJ IDEA plugin.

> **Note**: Currently only supports StarRocks SQL, other SQL syntax may not be compatible.

## ✨ Features

- **Intelligent Formatting**: Developed based on StarRocks lexical files
- **Rich Configuration Options**:
  - Indentation settings
  - Maximum line length control
  - Keyword case style
  - Comma position style
  - Function and expression parameter line breaks and alignment
  - CTE, JOIN, SELECT clause formatting
- **IntelliJ IDEA Plugin**

## 🚀 Quick Start

### Requirements

- Java 17+
- Maven 3.6+
- IntelliJ IDEA (optional, for plugin development)

### IDEA Plugin Usage

1. Build the plugin and install it in IDEA
2. Open the tool window (View → Tool Windows → StarSQLs)
3. Enter SQL code in the text area
4. Configure formatting options
5. Click "Format" or "Minify" button

## ⚙️ Configuration Options

| Option                | Type           | Default | Description                           |
|----------------------|----------------|---------|---------------------------------------|
| `indent`             | String         | "  "    | Indentation string                    |
| `maxLineLength`      | int            | 120     | Maximum line length                   |
| `keyWordStyle`       | KeyWordStyle   | UPPER   | Keyword style (UPPER/LOWER/ORIGINAL)  |
| `commaStyle`         | CommaStyle     | END     | Comma style (END/START)               |
| `breakFunctionArgs`  | boolean        | false   | Whether to break function arguments   |
| `alignFunctionArgs`  | boolean        | false   | Whether to align function arguments   |
| `breakCaseWhen`      | boolean        | false   | Whether to break CASE WHEN            |
| `alignCaseWhen`      | boolean        | false   | Whether to align CASE WHEN            |
| `breakInList`        | boolean        | false   | Whether to break IN lists             |
| `alignInList`        | boolean        | false   | Whether to align IN lists             |
| `breakAndOr`         | boolean        | false   | Whether to break AND/OR               |
| `breakCTE`           | boolean        | true    | Whether to break CTE                  |
| `breakJoinRelations` | boolean        | true    | Whether to break JOIN relations       |
| `breakSelectItems`   | boolean        | false   | Whether to break SELECT items         |
| `breakGroupByItems`  | boolean        | false   | Whether to break GROUP BY items       |
| `breakOrderBy`       | boolean        | false   | Whether to break ORDER BY             |
| `formatSubquery`     | boolean        | true    | Whether to format subqueries          |

## 📦 Dependencies

- **ANTLR4**: SQL syntax parsing
- **Guava**: Utility libraries
- **Apache Commons Lang3**: String processing
- **Gson**: JSON processing

## 🤝 Contributing

We welcome Issue submissions and Pull Requests!

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

For questions or suggestions, please contact us through:

- Submit an [Issue](https://github.com/your-repo/issues)
- Send email to: seaven_7@foxmail.com

--- 