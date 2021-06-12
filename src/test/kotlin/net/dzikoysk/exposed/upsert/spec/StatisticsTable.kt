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

import net.dzikoysk.exposed.upsert.withIndex
import net.dzikoysk.exposed.upsert.withUnique
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

internal object StatisticsTable : IntIdTable("statistics") {

    val type: Column<String> = varchar("type", 32)
    val value: Column<String> = varchar("value", 512)
    val count: Column<Long> = long("count")

    val typeIndex = withIndex("index_type", columns = arrayOf(type))
    val uniqueTypeValue = withUnique("unique_type_value", type, value)

}

/*
   Entity wit:h
   * Primary key on 'id'
   * Unique index on ('type', 'value') pair
   * Count to upsert
 */
internal data class Record(
    val id: Long = -1,
    val type: String,
    val value: String,
    val count: Long
) {

    override fun toString() = "$id | $type | $value | $count"

}