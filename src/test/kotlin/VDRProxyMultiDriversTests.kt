import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.PublicKey
import java.util.UUID
import proxy.VDRProxyMultiDrivers
import drivers.InMemoryDriver
import org.junit.jupiter.api.assertThrows
import urlManagers.LocalhostUrlManager

class VDRProxyMultiDriversTest {

    private lateinit var urlManager: LocalhostUrlManager
    private lateinit var driver1: InMemoryDriver
    private lateinit var driver2: InMemoryDriver
    private lateinit var vdrProxy: VDRProxyMultiDrivers

    @BeforeEach
    fun setUp() {
        urlManager = LocalhostUrlManager()
        driver1 = InMemoryDriver(
            identifier = "driver1",
            type = "typeA",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1")
        )
        driver2 = InMemoryDriver(
            identifier = "driver2",
            type = "typeB",
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
            vdrProxy.store("test data".toByteArray(), emptyMap())
        }

        assertEquals("The VDR does not have any driver associated", exception.message)
    }

    @Test
    fun `store should store data using single driver and return correct URL`() {
        val driver = InMemoryDriver(
            identifier = "driver1",
            type = "typeA",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1")
        )
        val vdrProxyTest = VDRProxyMultiDrivers(
            urlManager = LocalhostUrlManager(),
            drivers = arrayOf(driver),
            identifier = "vdrProxy",
            version = "1.0"
        )

        val data = "Sample Data".toByteArray()
        val resultUrl = vdrProxyTest.store(data, emptyMap())

        val storedUuid = resultUrl.split("?")[1].split("&").find { it.startsWith("dvrId=") }?.split("=")?.get(1)
        assertNotNull(storedUuid)
        assertFalse(driver.storage.isEmpty())
    }

    @Test
    fun `get should throw NoDriversException when no drivers are available`() {
        vdrProxy.drivers = arrayOf()

        val exception = assertThrows<VDRProxyMultiDrivers.NoDriversException> {
            vdrProxy.get("http://localhost/test/path")
        }

        assertEquals("The VDR does not have any driver associated", exception.message)
    }

    @Test
    fun `get should retrieve stored data using single driver`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver1.storage[uuid] = data

        val url = "http://localhost/path?dvrId=driver1#${uuid}"
        val result = vdrProxy.get(url)

        assertArrayEquals(data, result)
    }

    @Test
    fun `remove should delete stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver1.storage[uuid] = data

        val url = "http://localhost/path?dvrId=driver1#${uuid}"
        vdrProxy.remove(url, emptyMap())

        assertFalse(driver1.storage.containsKey(uuid))
    }

    @Test
    fun `remove should throw NoDriversException when no drivers are available`() {
        vdrProxy.drivers = arrayOf()

        val exception = assertThrows<VDRProxyMultiDrivers.NoDriversException> {
            vdrProxy.remove("http://localhost/path", emptyMap())
        }

        assertEquals("The VDR does not have any driver associated", exception.message)
    }
}