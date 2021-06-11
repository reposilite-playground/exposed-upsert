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
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

internal class StatisticsRepository {

    internal fun createSchema() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(StatisticsTable)
        }
    }

    internal fun upsertRecord(record: Record): Record =
        transaction {
            addLogger(StdOutSqlLogger)

            StatisticsTable.upsert(StatisticsTable.id,
                bodyInsert = {
                    it[this.type] = record.type
                    it[this.value] = record.value
                    it[this.count] = record.count
                },
                bodyUpdate = {
                    with(SqlExpressionBuilder) {
                        it.update(StatisticsTable.count, StatisticsTable.count + record.count)
                    }
                }
            )

            StatisticsTable.select(Op.build { StatisticsTable.type eq record.type }.and { StatisticsTable.value eq record.value })
                .first()
                .let { toRecord(it) }
        }

    internal fun findAll(): Collection<Record> =
        transaction {
            StatisticsTable.selectAll().map { toRecord(it) }
        }

    private fun toRecord(row: ResultRow): Record =
        Record(
            id = row[StatisticsTable.id].value.toLong(),
            type = row[StatisticsTable.type],
            value = row[StatisticsTable.value],
            count = row[StatisticsTable.count]
        )

}