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

package net.dzikoysk.exposed.upsert

import net.dzikoysk.exposed.upsert.spec.UpsertSpecification
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test
import kotlin.test.assertTrue

@Testcontainers
internal class PostgreSQLUpsertTest : UpsertSpecification() {

    private class SpecifiedPostgreSQLContainer(image: String) : PostgreSQLContainer<SpecifiedPostgreSQLContainer>(DockerImageName.parse(image))

    companion object {
        @Container
        private val POSTGRESQL_CONTAINER = SpecifiedPostgreSQLContainer("postgres:11.12")
    }

    @BeforeEach
    fun connect() {
        println(POSTGRESQL_CONTAINER.jdbcUrl)
        Database.connect(POSTGRESQL_CONTAINER.jdbcUrl, driver = "org.postgresql.Driver", user = "test", password = "test")
    }

    @Test
    fun `should launch database`() {
        assertTrue { POSTGRESQL_CONTAINER.isRunning }
    }

    @Test
    fun `should run upsert spec`() {
        super.shouldUpsertData()
    }

}