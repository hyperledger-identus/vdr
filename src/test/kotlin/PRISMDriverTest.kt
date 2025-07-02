import drivers.*
import fmgp.did.method.prism.*
import fmgp.did.method.prism.cardano.*
import fmgp.did.method.prism.vdr.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PRISMDriverTest {

    private lateinit var driver: PRISMDriver

    @BeforeEach
    fun setUp() {
        val bfConfig = BlockfrastConfig("preprod9EGSSMf6oWb81qoi8eW65iWaQuHJ1HwB")
        val mnemonic =
                scala.collection.JavaConverters.collectionAsScalaIterableConverter(
                                // scala.collection.JavaConverters.asJava(
                                listOf(
                                        "mention",
                                        "side",
                                        "album",
                                        "physical",
                                        "uncle",
                                        "lab",
                                        "horn",
                                        "nasty",
                                        "script",
                                        "few",
                                        "hazard",
                                        "announce",
                                        "upon",
                                        "group",
                                        "ten",
                                        "moment",
                                        "fantasy",
                                        "helmet",
                                        "supreme",
                                        "early",
                                        "gadget",
                                        "curve",
                                        "lecture",
                                        "edge"
                                )
                        )
                        .asScala()
                        .toSeq()
        val wallet = CardanoWalletConfig(mnemonic)
        driver = PRISMDriver(bfConfig, wallet)
    }

    @Test
    fun `my test`() {
        // assertThrows(PRISMDriver.DataCouldNotBeFoundException::class.java) {
        //     driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = null, options =
        // null)
        // }
        driver.test()
    }

    // @Test
    // fun `store should add data to storage and return a valid StoreResult`() {
    //     val data = "Sample Data".toByteArray()

    //     val result = driver.create(data, options = null)

    //     assertEquals(Driver.OperationState.SUCCESS, result.state)
    //     assertTrue(driver.storage.containsKey(result.fragment))
    //     assertArrayEquals(data, driver.storage[result.fragment])
    // }

    // @Test
    // fun `get should return stored data when a valid fragment is provided`() {
    //     val data = "Sample Data".toByteArray()
    //     val uuid = UUID.randomUUID().toString()
    //     driver.storage[uuid] = data

    //     val result = driver.read(paths = arrayOf(), queries = emptyMap(), fragment = uuid,
    // publicKeys = null)

    //     assertArrayEquals(data, result)
    // }

    // @Test
    // fun `get should throw DataCouldNotBeFoundException when fragment is null`() {
    //     assertThrows(InMemoryDriver.DataCouldNotBeFoundException::class.java) {
    //         driver.read(paths = arrayOf(), queries = emptyMap(), fragment = null, publicKeys =
    // null)
    //     }
    // }

    // @Test
    // fun `remove should delete stored data when a valid fragment is provided`() {
    //     val data = "Sample Data".toByteArray()
    //     val uuid = UUID.randomUUID().toString()
    //     driver.storage[uuid] = data

    //     driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = uuid, options = null)

    //     assertFalse(driver.storage.containsKey(uuid))
    // }

    // @Test
    // fun `remove should throw DataCouldNotBeFoundException when fragment is null`() {
    //     assertThrows(InMemoryDriver.DataCouldNotBeFoundException::class.java) {
    //         driver.delete(paths = arrayOf(), queries = emptyMap(), fragment = null, options =
    // null)
    //     }
    // }
}
