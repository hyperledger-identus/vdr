package org.hyperledger.identus.vdr

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.text.get
import org.hyperledger.identus.vdr.drivers.InMemoryDriver
import org.hyperledger.identus.vdr.interfaces.Driver

class InMemoryDriverTest {

    private lateinit var driver: InMemoryDriver

    @BeforeEach
    fun setUp() {
        driver = InMemoryDriver(
            identifier = "test-driver",
            family = "memory",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1")
        )
    }

    @Test
    fun `store should add data to storage and return a valid StoreResult`() {
        val data = "Sample Data".toByteArray()

        val result = driver.create(data, options = null)

        assertEquals(Driver.OperationState.SUCCESS, result.state)
        assertTrue(driver.storage.containsKey(result.fragment))
        assertArrayEquals(data, driver.storage[result.fragment])
    }

    @Test
    fun `get should return stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver.storage[uuid] = data

        val result = driver.read(paths = arrayOf(), queries = emptyMap(), fragment = uuid, publicKeys = null)

        assertArrayEquals(data, result)
    }

    @Test
    fun `get should throw DataCouldNotBeFoundException when fragment is null`() {
        assertThrows(InMemoryDriver.DataCouldNotBeFoundException::class.java) {
            driver.read(paths = arrayOf(), queries = emptyMap(), fragment = null, publicKeys = null)
        }
    }

    @Test
    fun `get should throw DataCouldNotBeFoundException when data is not found`() {
        assertThrows(InMemoryDriver.DataCouldNotBeFoundException::class.java) {
            driver.read(paths = arrayOf(), queries = emptyMap(), fragment = "hello", publicKeys = null)
        }
    }

    @Test
    fun `remove should delete stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver.storage[uuid] = data

        driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = uuid, options = null)

        assertFalse(driver.storage.containsKey(uuid))
    }

    @Test
    fun `remove should throw DataCouldNotBeFoundException when fragment is null`() {
        assertThrows(InMemoryDriver.DataCouldNotBeFoundException::class.java) {
            driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = null, options = null)
        }
    }
}