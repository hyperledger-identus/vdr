package drivers

import interfaces.Driver
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID

class InMemoryDriver(
    override val identifier: String,
    override val type: String,
    override val version: String,
    override val supportedVersions: Array<String>
) : Driver {

    class DataCouldNotBeFoundException: Exception("Could not find the data")

    var storage: MutableMap<String, ByteArray> = mutableMapOf()

    override fun store(
        data: ByteArray,
        privateKeys: Array<PrivateKey>?,
        metadata: Map<String, String>?
    ): Driver.StoreResult {
        val uuid = UUID.randomUUID().toString()
        storage += mutableMapOf(Pair(uuid, data))

        return  Driver.StoreResult(
            Driver.OperationState.SUCCESS,
            emptyArray(),
            emptyMap(),
            uuid,
            null
        )
    }

    override fun get(
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

    override fun remove(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        privateKeys: Array<PrivateKey>?,
        metadata: Map<String, String>?
    ) {
        if(fragment != null) {
            storage.remove(fragment)
        } else {
            throw DataCouldNotBeFoundException()
        }
    }
}