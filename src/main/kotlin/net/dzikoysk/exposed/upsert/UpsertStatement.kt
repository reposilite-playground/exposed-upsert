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

    // Merge arguments from INSERT + UPDATE statements
    override fun arguments(): List<List<Pair<IColumnType, Any?>>> {
        val updateArgs = updateStatement.arguments()

        return super.arguments().mapIndexed { index, iterable ->
            val list = iterable.toList()
            list + (updateArgs.getOrNull(index) ?: return@mapIndexed list)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun prepareSQL(transaction: Transaction) = buildString {
        append(super.prepareSQL(transaction))

        val updateValues = updateStatement.builderValues

        if (updateValues.isEmpty()) {
            return@buildString
        }

        val dialect = transaction.db.vendor

        if (dialect == "postgresql") {
            if (index) {
                append(" ON CONFLICT ON CONSTRAINT ")
                append(indexName)
            }
            else {
                append(" ON CONFLICT(")
                append(indexName)
                append(")")
            }

            append(" DO UPDATE SET ")
            updateValues.keys
                .filter { it !in indexColumns }
                .joinTo(this) { "${transaction.identity(it)}=EXCLUDED.${transaction.identity(it)}" }
        }
        else {
            append(" ON DUPLICATE KEY UPDATE ")
            updateValues.entries
                .filter { it.key !in indexColumns }
                .joinTo(this) { (column, value) ->
                    val queryBuilder = QueryBuilder(true)

                    when (value) {
                        is Expression<*> -> {
                            value.toQueryBuilder(queryBuilder)
                            "${transaction.identity(column)}=${queryBuilder}"
                        }
                        else -> "${transaction.identity(column)}=VALUES(${transaction.identity(column)})"
                    }
                }
        }
    }

}