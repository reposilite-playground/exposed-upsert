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
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test
import kotlin.test.assertTrue

@Testcontainers
internal class MySQLUpsertTest : UpsertSpecification() {

    private class SpecifiedMySQLContainer(image: String) : MySQLContainer<SpecifiedMySQLContainer>(DockerImageName.parse(image))

    companion object {
        @Container
        private val MYSQL_CONTAINER = SpecifiedMySQLContainer("mysql:8.0.25")
    }

    @BeforeEach
    fun connect() {
        println(MYSQL_CONTAINER.jdbcUrl)
        Database.connect(MYSQL_CONTAINER.jdbcUrl, driver = "com.mysql.cj.jdbc.Driver", user = "test", password = "test")
    }

    @Test
    fun `should launch database`() {
       assertTrue { MYSQL_CONTAINER.isRunning }
    }

    @Test
    fun `should run upsert spec`() {
        super.shouldUpsertData()
    }

}