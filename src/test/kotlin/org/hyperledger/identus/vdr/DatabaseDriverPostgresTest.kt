package org.hyperledger.identus.vdr

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.hyperledger.identus.vdr.drivers.DatabaseDriver
import org.hyperledger.identus.vdr.interfaces.Driver

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseDriverPostgresTest {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
    }

    private lateinit var dataSource: HikariDataSource
    private lateinit var driver: DatabaseDriver

    @BeforeEach
    fun setUp() {
        val hikariCfg = HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            maximumPoolSize = 2
            connectionTimeout = 20_000
        }
        dataSource = HikariDataSource(hikariCfg)
        driver = DatabaseDriver("pg-driver", "1.0", arrayOf("1.0", "1.1"), dataSource)
    }

    @AfterEach
    fun tearDown() {
        dataSource.close()
    }

    @Test
    fun `create should insert data and return SUCCESS`() {
        val payload = "Hello PostgreSQL".toByteArray()
        val result = driver.create(payload, null)
        Assertions.assertEquals(Driver.OperationState.SUCCESS, result.state)
        Assertions.assertArrayEquals(payload, driver.read(emptyArray(), emptyMap(), result.fragment, null))
    }

    @Test
    fun `update should overwrite data`() {
        val id = driver.create("foo".toByteArray(), null).fragment
        val newPayload = "bar".toByteArray()
        driver.update(newPayload, emptyArray(), emptyMap(), id, null)
        Assertions.assertArrayEquals(newPayload, driver.read(emptyArray(), emptyMap(), id, null))
    }

    @Test
    fun `delete should remove data`() {
        val id = driver.create("will‑delete".toByteArray(), null).fragment
        driver.delete(emptyArray(), emptyMap(), id, null)
        Assertions.assertThrows(DatabaseDriver.DataCouldNotBeFoundException::class.java) {
            driver.read(emptyArray(), emptyMap(), id, null)
        }
    }
}