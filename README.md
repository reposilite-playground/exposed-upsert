# Exposed Upsert [![CI](https://github.com/dzikoysk/exposed-upsert/actions/workflows/gradle.yml/badge.svg)](https://github.com/dzikoysk/exposed-upsert/actions/workflows/gradle.yml)
Upsert dsl extension for Exposed, Kotlin SQL framework 

## Coverage

Supported databases with tests run against real databases using [Testcontainers](https://www.testcontainers.org/):

| DB                 | Status          |
|--------------------|-----------------|
| H2                 | Unsupported     |
| H2 (MySQL Dialect) | ✅               |
| MySQL              | ✅               |
| MariaDB            | ✅               |
| Oracle             | Not implemented |
| PostgreSQL         | ✅               |
| SQL Server         | Unsupported     |
| SQLite             | ✅              |