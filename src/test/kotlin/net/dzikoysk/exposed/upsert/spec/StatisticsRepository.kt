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

import net.dzikoysk.exposed.shared.IdentifiableEntity
import net.dzikoysk.exposed.shared.UNINITIALIZED_ENTITY_ID
import net.dzikoysk.exposed.upsert.upsert
import net.dzikoysk.exposed.upsert.withIndex
import net.dzikoysk.exposed.upsert.withUnique
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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

internal class StatisticsRepository {

    internal object StatisticsTable : IntIdTable("statistics") {

        val httpMethod: Column<String> = varchar("http_method", 32)
        val uri: Column<String> = varchar("uri", 512)
        val count: Column<Long> = long("count")

        val typeIndex = withIndex("index_type", columns = arrayOf(httpMethod))
        val uniqueTypeValue = withUnique("unique_http_method_to_uri", httpMethod, uri)

    }

    /*
       Entity wit:h
       * Primary key on 'id'
       * Unique index on ('type', 'value') pair
       * Count to upsert
     */
    internal data class Record(
        override val id: Int = UNINITIALIZED_ENTITY_ID,
        val httpMethod: String,
        val uri: String,
        val count: Long
    ) : IdentifiableEntity {

        override fun toString() = "$id | $httpMethod | $uri | $count"

    }

    internal fun createSchema() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(StatisticsTable)
        }
    }

    internal fun upsertRecord(record: Record): Record =
        transaction {
            addLogger(StdOutSqlLogger)

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

            findByTypeAndValue(record.httpMethod, record.uri)
        }

    internal fun findByTypeAndValue(type: String, value: String): Record =
        transaction {
            StatisticsTable.select(Op.build { StatisticsTable.httpMethod eq type }.and { StatisticsTable.uri eq value })
                .first()
                .let { toRecord(it) }
        }

    internal fun findAll(): Collection<Record> =
        transaction {
            StatisticsTable.selectAll().map { toRecord(it) }
        }

    private fun toRecord(row: ResultRow): Record =
        Record(
            id = row[StatisticsTable.id].value,
            httpMethod = row[StatisticsTable.httpMethod],
            uri = row[StatisticsTable.uri],
            count = row[StatisticsTable.count]
        )

}