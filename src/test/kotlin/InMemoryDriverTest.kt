import drivers.InMemoryDriver
import interfaces.Driver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryDriverTest {

    private lateinit var driver: InMemoryDriver

    @BeforeEach
    fun setUp() {
        driver = InMemoryDriver(
            identifier = "test-driver",
            type = "memory",
            version = "1.0",
            supportedVersions = arrayOf("1.0", "1.1")
        )
    }

    @Test
    fun `store should add data to storage and return a valid StoreResult`() {
        val data = "Sample Data".toByteArray()

        val result = driver.create(data, metadata = null)

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
    fun `remove should delete stored data when a valid fragment is provided`() {
        val data = "Sample Data".toByteArray()
        val uuid = UUID.randomUUID().toString()
        driver.storage[uuid] = data

        driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = uuid, metadata = null)

        assertFalse(driver.storage.containsKey(uuid))
    }

    @Test
    fun `remove should throw DataCouldNotBeFoundException when fragment is null`() {
        assertThrows(InMemoryDriver.DataCouldNotBeFoundException::class.java) {
            driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = null, metadata = null)
        }
    }
}