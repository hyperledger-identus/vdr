package org.hyperledger.identus.vdr

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.*
import java.util.UUID
import org.hyperledger.identus.vdr.drivers.DatabaseDriver
import org.hyperledger.identus.vdr.interfaces.Driver

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DatabaseDriverHikariTest {

    private lateinit var dataSource: HikariDataSource
    private lateinit var driver: DatabaseDriver

    @BeforeEach
    fun setUp() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:test-${UUID.randomUUID()}?DB_CLOSE_DELAY=-1"
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 2
        }
        dataSource = HikariDataSource(config)

        driver = DatabaseDriver(
            identifier = "test-db-driver",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1"),
            dataSource = dataSource
        )
    }

    @AfterEach
    fun tearDown() {
        dataSource.close()
    }

    @Test
    fun `create should insert data and return a valid OperationResult`() {
        val data = "Sample Data".toByteArray()

        val result = driver.create(data, options = null)

        Assertions.assertEquals(Driver.OperationState.SUCCESS, result.state)
        val stored = driver.read(paths = emptyArray(), queries = emptyMap(), fragment = result.fragment, publicKeys = null)
        Assertions.assertArrayEquals(data, stored)
    }

    @Test
    fun `read should return stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val createResult = driver.create(data, options = null)

        val readBack = driver.read(paths = emptyArray(), queries = emptyMap(), fragment = createResult.fragment, publicKeys = null)
        Assertions.assertArrayEquals(data, readBack)
    }

    @Test
    fun `read should throw DataCouldNotBeFoundException when fragment is null`() {
        Assertions.assertThrows(DatabaseDriver.DataCouldNotBeFoundException::class.java) {
            driver.read(paths = emptyArray(), queries = emptyMap(), fragment = null, publicKeys = null)
        }
    }

    @Test
    fun `update should overwrite existing data and return SUCCESS`() {
        val initial = "Initial".toByteArray()
        val updated = "Updated".toByteArray()
        val createResult = driver.create(initial, options = null)

        val updateResult = driver.update(
            data = updated,
            paths = emptyArray(),
            queries = emptyMap(),
            fragment = createResult.fragment,
            options = null
        )

        Assertions.assertEquals(Driver.OperationState.SUCCESS, updateResult.state)
        val readBack = driver.read(paths = emptyArray(), queries = emptyMap(), fragment = createResult.fragment, publicKeys = null)
        Assertions.assertArrayEquals(updated, readBack)
    }

    @Test
    fun `update should throw DataCouldNotBeFoundException when fragment is null`() {
        Assertions.assertThrows(DatabaseDriver.DataCouldNotBeFoundException::class.java) {
            driver.update(
                data = ByteArray(0),
                paths = emptyArray(),
                queries = emptyMap(),
                fragment = null,
                options = null
            )
        }
    }

    @Test
    fun `delete should remove stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val createResult = driver.create(data, options = null)

        driver.delete(paths = emptyArray(), queries = emptyMap(), fragment = createResult.fragment, options = null)

        Assertions.assertThrows(DatabaseDriver.DataCouldNotBeFoundException::class.java) {
            driver.read(paths = emptyArray(), queries = emptyMap(), fragment = createResult.fragment, publicKeys = null)
        }
    }

    @Test
    fun `delete should throw DataCouldNotBeFoundException when fragment is null`() {
        Assertions.assertThrows(DatabaseDriver.DataCouldNotBeFoundException::class.java) {
            driver.delete(paths = emptyArray(), queries = emptyMap(), fragment = null, options = null)
        }
    }

    @Test
    fun `verify should return proof and optionally data`() {
        val data = "Proof Data".toByteArray()
        val createResult = driver.create(data, options = null)

        val proofWithData = driver.verify(
            paths = emptyArray(),
            queries = emptyMap(),
            fragment = createResult.fragment,
            publicKeys = null,
            returnData = true
        )
        Assertions.assertArrayEquals(data, proofWithData.data)

        val proofWithoutData = driver.verify(
            paths = emptyArray(),
            queries = emptyMap(),
            fragment = createResult.fragment,
            publicKeys = null,
            returnData = false
        )
        Assertions.assertNull(proofWithoutData.data)
    }
}