package net.dzikoysk.exposed.upsert

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

typealias UpsertBody<T> = T.(InsertStatement<Number>) -> Unit

/**
 * Compiles an upsert statement for the given table.
 * Use conflictColumn or conflictIndex to specify the column or index to check for conflicts. (use only one)
 * You can use the insertBody and updateBody to specify the values for the insert and update statements.
 *
 * @param conflictColumn column to check for conflicts
 * @param conflictIndex index to check for conflicts
 * @param insertBody body of the insert statement
 * @param updateBody body of the update statement
 */
fun <T : Table> T.upsert(conflictColumn: Column<*>? = null, conflictIndex: Index? = null, insertBody: UpsertBody<T>, updateBody: UpsertBody<T>) {
    val updateStatement = UpsertInsertStatement<Number>(this)
    updateBody(this, updateStatement)

    UpsertStatement<Number>(this, conflictColumn, conflictIndex, updateStatement).apply {
        insertBody(this)
        execute(TransactionManager.current())
    }
}

/**
 * Creates named index on the table.
 */
fun Table.withIndex(customIndexName: String? = null, isUnique: Boolean = false, vararg columns: Column<*>): Index {
    val index = Index(columns.toList(), isUnique, customIndexName)
    val indices: MutableList<Index> = this.indices as MutableList<Index>
    indices.add(index)
    return index
}

/**
 * Creates named unique index on the table.
 */
fun Table.withUnique(customIndexName: String? = null, vararg columns: Column<*>): Index =
    withIndex(customIndexName, true, *columns)