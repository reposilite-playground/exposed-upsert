package net.dzikoysk.exposed.upsert

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

typealias UpsertBody<T> = T.(InsertStatement<Number>) -> Unit

fun <T : Table> T.upsert(conflictColumn: Column<*>? = null, conflictIndex: Index? = null, bodyInsert: UpsertBody<T>, bodyUpdate: UpsertBody<T>) {
    val updateStatement = UpsertInsertStatement<Number>(this)
    bodyUpdate(this, updateStatement)

    UpsertStatement<Number>(this, conflictColumn, conflictIndex, updateStatement).apply {
        bodyInsert(this)
        execute(TransactionManager.current())
    }
}

fun Table.withIndex(customIndexName: String? = null, isUnique: Boolean = false, vararg columns: Column<*>): Index {
    val index = Index(columns.toList(), isUnique, customIndexName)
    val indices: MutableList<Index> = this.indices as MutableList<Index>
    indices.add(index)
    return index
}

fun Table.withUnique(customIndexName: String? = null, vararg columns: Column<*>): Index =
    withIndex(customIndexName, true, *columns)