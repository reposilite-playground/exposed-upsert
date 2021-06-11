/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */

package net.dzikoysk.exposed.upsert.spec

import net.dzikoysk.exposed.upsert.upsert
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

internal object StatisticsTable : IntIdTable("statistics") {

    val type: Column<String> = varchar("type", 32)
    val identifier: Column<String> = varchar("identifier", 32)
    val count: Column<Long> = long("count")

    init {
        index("index_type", columns = arrayOf(type))
        uniqueIndex("unique_type_identifier", type, identifier)
    }

}

internal fun createSchema() {
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(StatisticsTable)
    }
}

internal data class Record(
    val id: Long = -1,
    val type: String,
    val identifier: String,
    val count: Long
) {

    override fun toString() = "$id | $type | $identifier | $count"

}

internal fun upsertRecord(record: Record): Record =
    transaction {
        addLogger(StdOutSqlLogger)

        StatisticsTable.upsert(StatisticsTable.id,
            bodyInsert = {
                it[this.type] = record.type
                it[this.identifier] = record.identifier
                it[this.count] = record.count
            },
            bodyUpdate = {
                with(SqlExpressionBuilder) {
                    it.update(StatisticsTable.count, StatisticsTable.count + 4)
                }
            }
        )

        StatisticsTable.select(Op.build { StatisticsTable.type eq record.type }.and { StatisticsTable.identifier eq record.identifier })
            .first()
            .let { toRecord(it) }
    }

private fun toRecord(row: ResultRow): Record =
    Record(
        id = row[StatisticsTable.id].value.toLong(),
        type = row[StatisticsTable.type],
        identifier = row[StatisticsTable.identifier],
        count = row[StatisticsTable.count]
    )