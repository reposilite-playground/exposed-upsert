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
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

internal class CountRepository {

    internal object CountTable : IntIdTable("count") {
        val count: Column<Int> = integer("count")
        val state: Column<String> = text("state")
    }

    internal fun createSchema() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(CountTable)
        }
    }

    internal fun upsertCount(id: Int, count: Int): Pair<Int, String> =
        transaction {
            CountTable.upsert(CountTable.id,
                insertBody = {
                    it[CountTable.id] = id
                    it[CountTable.count] = count
                    it[CountTable.state] = "inserted"
                },
                updateBody = {
                    with(SqlExpressionBuilder) {
                        it.update(CountTable.count, CountTable.count + count)
                    }

                    it[CountTable.state] = "updated"
                }
            )

            CountTable.select { CountTable.id eq id }
                .first()
                .let {
                    Pair(
                        it[CountTable.count],
                        it[CountTable.state]
                    )
                }
        }

}