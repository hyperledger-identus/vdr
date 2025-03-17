package drivers

import interfaces.Driver
import interfaces.Proof
import java.security.PublicKey
import java.util.UUID

/**
 * InMemoryDriver is an example implementation of the [Driver] interface.
 * It stores data in a mutable map in memory. This implementation is useful for testing or scenarios
 * where persistent storage is not required.
 *
 * @property storage A mutable map that holds the stored data.
 */
class InMemoryDriver(
    override val identifier: String,
    override val family: String,
    override val version: String,
    override val supportedVersions: Array<String>
) : Driver {

    /**
     * Exception thrown when data cannot be found in the storage.
     */
    class DataCouldNotBeFoundException: Exception("Could not find the data")

    var storage: MutableMap<String, ByteArray> = mutableMapOf()

    override fun create(
        data: ByteArray,
        metadata: Map<String, String>?
    ): Driver.OperationResult {
        val uuid = UUID.randomUUID().toString()
        storage += mutableMapOf(Pair(uuid, data))

        return Driver.OperationResult(
            UUID.randomUUID().toString(),
            Driver.OperationState.SUCCESS,
            emptyArray(),
            mapOf(Pair("blockNumber", "0000000000")),
            uuid,
            null,
            null
        )
    }

    override fun update(
        data: ByteArray,
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        metadata: Map<String, String>?
    ): Driver.OperationResult {
        if(fragment != null) {
            storage += mutableMapOf(Pair(fragment, data))

            return Driver.OperationResult(
                UUID.randomUUID().toString(),
                Driver.OperationState.SUCCESS,
                emptyArray(),
                mapOf(Pair("blockNumber", "0000000000")),
                fragment,
                null,
                null
            )
        } else {
            throw DataCouldNotBeFoundException()
        }
    }

    override fun read(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): ByteArray {
        if(fragment != null) {
            return storage.getValue(fragment)
        } else {
            throw DataCouldNotBeFoundException()
        }
    }

    override fun delete(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        metadata: Map<String, String>?
    ) {
        if(fragment != null) {
            storage.remove(fragment)
        } else {
            throw DataCouldNotBeFoundException()
        }
    }

    override fun verify(paths: Array<String>,
               queries: Map<String, String>,
               fragment: String?,
               publicKeys: Array<PublicKey>?,
               returnData: Boolean
    ): Proof {
        val dataRead = read(paths, queries, fragment, publicKeys)
        val sendData = if (returnData) { dataRead } else { null }
        return Proof(
            "SHA256",
            sendData,
            ByteArray(0) // Empty SHA256 for now we dont want to implement
        )
    }

    override fun storeResultState(identifier: String): Driver.OperationState {
        return Driver.OperationState.SUCCESS
    }
}