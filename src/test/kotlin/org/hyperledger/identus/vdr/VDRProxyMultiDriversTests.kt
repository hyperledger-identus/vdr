package org.hyperledger.identus.vdr

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import org.junit.jupiter.api.assertThrows
import org.hyperledger.identus.vdr.proxy.VDRProxyMultiDrivers
import org.hyperledger.identus.vdr.drivers.InMemoryDriver
import org.hyperledger.identus.vdr.urlManagers.BaseUrlManager

class VDRProxyMultiDriversTest {

    private lateinit var urlManager: BaseUrlManager
    private lateinit var driver1: InMemoryDriver
    private lateinit var driver2: InMemoryDriver
    private lateinit var vdrProxy: VDRProxyMultiDrivers

    @BeforeEach
    fun setUp() {
        urlManager = BaseUrlManager("http://localhost")
        driver1 = InMemoryDriver(
            identifier = "driver1",
            family = "cardano",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1")
        )
        driver2 = InMemoryDriver(
            identifier = "driver2",
            family = "typeB",
            version = "2.0",
            supportedVersions = arrayOf("2.0")
        )
        vdrProxy = VDRProxyMultiDrivers(
            urlManager = urlManager,
            drivers = arrayOf(driver1, driver2),
            identifier = "vdrProxy",
            version = "1.0"
        )
    }

    @Test
    fun `store should throw NoDriversException when no drivers are available`() {
        vdrProxy.drivers = arrayOf()

        val exception = assertThrows<VDRProxyMultiDrivers.NoDriversException> {
            vdrProxy.create("test data".toByteArray(), emptyMap())
        }

        assertEquals("The VDR does not have any driver associated", exception.message)
    }

    @Test
    fun `store should store data using single driver and return correct URL`() {
        val driver = InMemoryDriver(
            identifier = "driver1",
            family = "cardano",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1")
        )
        val vdrProxyTest = VDRProxyMultiDrivers(
            urlManager = BaseUrlManager("http://localhost"),
            drivers = arrayOf(driver),
            identifier = "vdrProxy",
            version = "1.0"
        )

        val data = "Sample Data".toByteArray()
        val resultUrl = vdrProxyTest.create(data, emptyMap())

        val storedUuid = resultUrl.split("?")[1].split("&").find { it.startsWith("drid=") }?.split("=")?.get(1)
        assertNotNull(storedUuid)
        assertFalse(driver.storage.isEmpty())
    }

    @Test
    fun `get should throw NoDriversException when no drivers are available`() {
        vdrProxy.drivers = arrayOf()

        val exception = assertThrows<VDRProxyMultiDrivers.NoDriversException> {
            vdrProxy.read("http://localhost/test/path")
        }

        assertEquals("The VDR does not have any driver associated", exception.message)
    }

    @Test
    fun `get should retrieve stored data using single driver`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver1.storage[uuid] = data

        val url = "http://localhost/path?drid=driver1#${uuid}"
        val result = vdrProxy.read(url)

        assertArrayEquals(data, result)
    }

    @Test
    fun `remove should delete stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver1.storage[uuid] = data

        val url = "http://localhost/path?drid=driver1#${uuid}"
        vdrProxy.delete(url, emptyMap())

        assertFalse(driver1.storage.containsKey(uuid))
    }

    @Test
    fun `remove should throw NoDriversException when no drivers are available`() {
        vdrProxy.drivers = arrayOf()

        val exception = assertThrows<VDRProxyMultiDrivers.NoDriversException> {
            vdrProxy.delete("http://localhost/path", emptyMap())
        }

        assertEquals("The VDR does not have any driver associated", exception.message)
    }
}