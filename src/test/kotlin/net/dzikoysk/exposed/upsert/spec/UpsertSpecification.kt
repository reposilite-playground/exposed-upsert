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

import net.dzikoysk.exposed.shared.UNINITIALIZED_ENTITY_ID
import net.dzikoysk.exposed.upsert.spec.StatisticsRepository.Record
import kotlin.test.assertEquals

internal open class UpsertSpecification {

    private val countRepository = CountRepository()
    private val statisticsRepository = StatisticsRepository()

    internal fun shouldUpsertData() {
        shouldUpsertValueByColumn()
        shouldUpsertRecordsByUniqueIndex()
    }

    private fun shouldUpsertValueByColumn() {
        countRepository.createSchema()

        // should create records
        assertEquals(Pair(3, "inserted"), countRepository.upsertCount(1, 3))
        assertEquals(Pair(3, "inserted"), countRepository.upsertCount(2, 3))

        // should upsert records
        assertEquals(Pair(6, "updated"), countRepository.upsertCount(1, 3))
        assertEquals(Pair(6, "updated"), countRepository.upsertCount(2, 3))
    }

    private fun shouldUpsertRecordsByUniqueIndex() {
        statisticsRepository.createSchema()

        // should create records and generate id
        assertEquals(1, statisticsRepository.upsertRecord(Record(UNINITIALIZED_ENTITY_ID, "GET", "/xyz", 3)).id)
        assertEquals(2, statisticsRepository.upsertRecord(Record(UNINITIALIZED_ENTITY_ID, "GET", "/xyz/xyz", 30)).id)
        assertEquals(3, statisticsRepository.upsertRecord(Record(UNINITIALIZED_ENTITY_ID, "POST", "/xyz/xyz", 300)).id)

        statisticsRepository.findAll().forEach { println(it) }

        // should upsert records that match (httpMethod, uri) unique index
        assertEquals(1, statisticsRepository.upsertRecord(Record(UNINITIALIZED_ENTITY_ID, "GET", "/xyz", 1)).id)
        assertEquals(2, statisticsRepository.upsertRecord(Record(UNINITIALIZED_ENTITY_ID, "GET", "/xyz/xyz", 10)).id)
        assertEquals(3, statisticsRepository.upsertRecord(Record(UNINITIALIZED_ENTITY_ID, "POST", "/xyz/xyz", 100)).id)

        statisticsRepository.findAll().forEach { println(it) }

        // should upsert records by unique index with specified id
        assertEquals(4, statisticsRepository.upsertRecord(Record(1, "GET", "/xyz", 0)).count)
        assertEquals(40, statisticsRepository.upsertRecord(Record(2, "GET", "/xyz/xyz", 0)).count)
        assertEquals(400, statisticsRepository.upsertRecord(Record(3, "POST", "/xyz/xyz", 0)).count)

        statisticsRepository.findAll().forEach { println(it) }
    }

}