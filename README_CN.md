# StarSQLs - StarRocks SQL Formatter

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Version](https://img.shields.io/badge/Version-1.0-green.svg)](CHANGELOG.md)

StarSQLs 是一个为 StarRocks SQL 开发的工具集合，目前提供 SQL 格式化功能。相比其他格式化工具，针对 SQL 各个子句支持更丰富的格式化选项，同时支持
命令行， WEB端 和 IntelliJ IDEA 插件。

> Tips: 目前仅支持 StarRocks SQL，其他 SQL 语法不一定兼容.

## ✨ 功能特性

- **智能格式化**: 基于 StarRocks 的词法文件开发。
- **支持丰富的配置选项**:
  - 缩进设置
  - 最大行长度控制
  - 关键字大小写样式
  - 逗号位置样式
  - 函数、表达式参数换行和对齐
  - CTE、JOIN、SELECT 等子句格式化
- **IntelliJ IDEA 插件**

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- IntelliJ IDEA (可选，用于插件开发)

### IDEA 插件使用

1. 构建插件后，在 IDEA 中安装插件
2. 打开工具窗口 (View → Tool Windows → StarSQLs)
3. 在文本区域输入 SQL 代码
4. 配置格式化选项
5. 点击 "Format" 或 "Minify" 按钮

## ⚙️ 配置选项

| 选项                   | 类型           | 默认值   | 描述                           |
|----------------------|--------------|-------|------------------------------|
| `indent`             | String       | "  "  | 缩进字符串                        |
| `maxLineLength`      | int          | 120   | 最大行长度                        |
| `keyWordStyle`       | KeyWordStyle | UPPER | 关键字样式 (UPPER/LOWER/ORIGINAL) |
| `commaStyle`         | CommaStyle   | END   | 逗号样式 (END/START)             |
| `breakFunctionArgs`  | boolean      | false | 函数参数是否换行                     |
| `alignFunctionArgs`  | boolean      | false | 函数参数是否对齐                     |
| `breakCaseWhen`      | boolean      | false | CASE WHEN 是否换行               |
| `alignCaseWhen`      | boolean      | false | CASE WHEN 是否对齐               |
| `breakInList`        | boolean      | false | IN 列表是否换行                    |
| `alignInList`        | boolean      | false | IN 列表是否对齐                    |
| `breakAndOr`         | boolean      | false | AND/OR 是否换行                  |
| `breakCTE`           | boolean      | true  | CTE 是否换行                     |
| `breakJoinRelations` | boolean      | true  | JOIN 关系是否换行                  |
| `breakSelectItems`   | boolean      | false | SELECT 项是否换行                 |
| `breakGroupByItems`  | boolean      | false | GROUP BY 项是否换行               |
| `breakOrderBy`       | boolean      | false | ORDER BY 是否换行                |
| `formatSubquery`     | boolean      | true  | 是否格式化子查询                     |

## 📦 依赖

- **ANTLR4**: SQL 语法解析
- **Guava**: 工具类库
- **Apache Commons Lang3**: 字符串处理
- **Gson**: JSON 处理

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](https://github.com/your-repo/issues)
- 发送邮件至: seaven_7@foxmail.com

---