import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import urlManagers.LocalhostUrlManager

class LocalhostUrlManagerTest {

    private val urlManager = LocalhostUrlManager()

    @Test
    fun `createNew should construct URL without fragment`() {
        val paths = arrayOf("api", "v1", "resource")
        val queries = mapOf("param1" to "value1", "param2" to "value2")
        val fragment: String? = null

        val result = urlManager.create(paths, queries, fragment, null)

        assertEquals("http://localhost/api/v1/resource?param1=value1&param2=value2", result)
    }

    @Test
    fun `createNew should construct URL with fragment`() {
        val paths = arrayOf("api", "v1", "resource")
        val queries = mapOf("param1" to "value1", "param2" to "value2")
        val fragment = "section1"

        val result = urlManager.create(paths, queries, fragment, null)

        assertEquals("http://localhost/api/v1/resource?param1=value1&param2=value2#section1", result)
    }

    @Test
    fun `resolve should parse URL into URLManager_URL object without fragment`() {
        val url = "http://localhost/api/v1/resource?param1=value1&param2=value2"
        val result = urlManager.resolve(url)

        assertEquals(mapOf("param1" to "value1", "param2" to "value2"), result.queries)
        assertEquals(null, result.fragment)
    }

    @Test
    fun `resolve should parse URL into URLManager_URL object with fragment`() {
        val url = "http://localhost/api/v1/resource?param1=value1&param2=value2#section1"
        val result = urlManager.resolve(url)

        assertEquals(mapOf("param1" to "value1", "param2" to "value2"), result.queries)
        assertEquals("section1", result.fragment)
    }

    @Test
    fun `createNew should handle empty paths and queries`() {
        val paths = arrayOf<String>()
        val queries = mapOf<String, String>()
        val fragment: String? = null

        val result = urlManager.create(paths, queries, fragment, null)

        assertEquals("http://localhost/?", result)
    }
}