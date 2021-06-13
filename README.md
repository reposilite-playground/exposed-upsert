# Exposed Upsert [![CI](https://github.com/dzikoysk/exposed-upsert/actions/workflows/gradle.yml/badge.svg)](https://github.com/dzikoysk/exposed-upsert/actions/workflows/gradle.yml) [![codecov](https://codecov.io/gh/dzikoysk/exposed-upsert/branch/main/graph/badge.svg?token=dIBANIssGf)](https://codecov.io/gh/dzikoysk/exposed-upsert)
Upsert DSL extension for Exposed, Kotlin SQL framework.
Project bases on various solutions provided by community in the official _["Exposed: Support upsert functionality"](https://github.com/JetBrains/Exposed/issues/167)_ feature request.
After 4 years, maintainers still didn't provide a solution, so here's a straightforward alternative.

* Implements all dialects that support native upsert possibilities 
* Tested against real databased through dedicated Docker containers provided by [Testcontainers](https://www.testcontainers.org/)
* Licensed to public domain, you can do whatever you want with sources in this repository

### Coverage

Supported databases with tests run against real databases using [Testcontainers](https://www.testcontainers.org/):

| DB                 | Status          |
|--------------------|-----------------|
| H2                 | Unsupported     |
| H2 (MySQL Dialect) | ✅               |
| MySQL              | ✅               |
| MariaDB            | ✅               |
| Oracle             | Not implemented _(Licensed to enterprise)_ |
| PostgreSQL         | ✅               |
| SQL Server         | Unsupported     |
| SQLite             | ✅              |

### Usage

```kotlin
class StatisticsTable : Table("statistics") {
    // [...]
    
    val uniqueTypeValue = withUnique("unique_http_method_to_uri", httpMethod, uri)
}

StatisticsTable.upsert(conflictIndex = StatisticsTable.uniqueTypeValue,
    insertBody = {
        it[this.httpMethod] = record.httpMethod
        it[this.uri] = record.uri
        it[this.count] = record.count
    },
    updateBody = {
        with(SqlExpressionBuilder) {
            it.update(StatisticsTable.count, StatisticsTable.count + record.count)
        }
    }
)
```

**Notes**
* Upsert functionality between _(MySQL, MariaDB, H2 with MySQL dialect)_ and _(PostgreSQL, SQLite)_ are slightly different.
  To keep the compatibility between these databases, you should always use only one condition of uniqueness (unique column OR unique index).
  MySQL based dialects may handle multiple queries due to the better support provided by generic `ON DUPLICATE KEY` query. 

### Download

#### Gradle

```groovy
repositories {
    maven { url 'https://repo.panda-lang.org/releases' }
}

dependencies {
    implementation 'net.dzikoysk:exposed-upsert:1.0.0'
}
```

#### Manual

You can find all available versions in the repository:

* [Repository - Artifact net.dzikoysk:exposed-upsert](https://repo.panda-lang.org/net/dzikoysk/exposed-upsert)

### Who's using
* [Reposilite](https://github.com/dzikoysk/reposilite)