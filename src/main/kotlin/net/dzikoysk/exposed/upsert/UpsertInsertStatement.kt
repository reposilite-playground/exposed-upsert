package net.dzikoysk.exposed.upsert

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement

internal class UpsertInsertStatement<Key : Any>(table: Table, isIgnore: Boolean = false) : InsertStatement<Key>(table, isIgnore) {
    internal val builderValues: Map<Column<*>, Any?> = super.values
}