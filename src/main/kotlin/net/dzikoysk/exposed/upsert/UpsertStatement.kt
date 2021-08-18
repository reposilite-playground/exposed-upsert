package net.dzikoysk.exposed.upsert

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement

internal class UpsertStatement<Key : Any>(
    table: Table,
    conflictColumn: Column<*>? = null,
    conflictIndex: Index? = null,
    private val updateStatement: UpsertInsertStatement<Number>
) : InsertStatement<Key>(table, false) {

    private val indexName: String
    private val indexColumns: List<Column<*>>
    private val index: Boolean

    init {
        when {
            conflictIndex != null -> {
                index = true
                indexName = conflictIndex.indexName
                indexColumns = conflictIndex.columns
            }
            conflictColumn != null -> {
                index = false
                indexName = conflictColumn.name
                indexColumns = listOf(conflictColumn)
            }
            else -> throw IllegalArgumentException()
        }
    }

    // Merge arguments from INSERT + UPDATE statements in sublist
    override fun arguments(): List<List<Pair<IColumnType, Any?>>> {
        val updateArgs = updateStatement.arguments()

        val result = super.arguments().mapIndexed { index, iterable ->
            val list = iterable.toList()
            list + (updateArgs.getOrNull(index) ?: return@mapIndexed list)
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun prepareSQL(transaction: Transaction) = buildString {
        append(super.prepareSQL(transaction))

        val updateValues = updateStatement.builderValues

        if (updateValues.isEmpty()) {
            return@buildString
        }

        val upsert = when (val dialect = transaction.db.vendor) {
            "mysql", "mariadb", "h2" -> onDuplicateKeyUpdate(transaction, updateValues)
            "postgresql" -> {
                if (index) {
                    append(" ON CONFLICT ON CONSTRAINT $indexName")
                } else {
                    append(" ON CONFLICT($indexName)")
                }

                doUpdateSet(transaction, updateValues)
            }
            "sqlite" -> {
                append(" ON CONFLICT(")

                append(indexColumns.joinToString(",") { '"' + it.name + '"' })

                append(")")
                doUpdateSet(transaction, updateValues)
            }
            else -> throw UnsupportedOperationException("Unsupported SQL dialect: $dialect")
        }

        append(upsert)
    }

    private fun doUpdateSet(transaction: Transaction, updateValues: Map<Column<*>, Any?>): String = buildString {
        append(" DO UPDATE SET ")

        updateValues.entries
            .filter { it.key !in indexColumns }
            .joinTo(this) { (column, value) ->
                when (value) {
                    is Expression<*> -> {
                        val queryBuilder = QueryBuilder(true)
                        value.toQueryBuilder(queryBuilder)
                        "${transaction.identity(column)}=${queryBuilder}"
                    }
                    else -> "${transaction.identity(column)}=?"
                }
            }
    }

    private fun onDuplicateKeyUpdate(transaction: Transaction, updateValues: Map<Column<*>, Any?>): String = buildString {
        append(" ON DUPLICATE KEY UPDATE ")

        updateValues.entries
            .filter { it.key !in indexColumns }
            .joinTo(this) { (column, value) ->
                when (value) {
                    is Expression<*> -> {
                        val queryBuilder = QueryBuilder(true)
                        value.toQueryBuilder(queryBuilder)
                        "${transaction.identity(column)}=${queryBuilder}"
                    }
                    else -> "${transaction.identity(column)}=?"
                }
            }
    }

}