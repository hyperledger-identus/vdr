package org.hyperledger.identus.vdr.drivers

import org.hyperledger.identus.vdr.interfaces.Driver
import org.hyperledger.identus.vdr.interfaces.Proof
import java.security.PublicKey
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * InMemoryDriver is an example implementation of the [org.hyperledger.identus.vdr.interfaces.Driver] interface.
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

    val storage: ConcurrentMap<String, ByteArray> = ConcurrentHashMap()

    override fun create(
        data: ByteArray,
        options: Map<String, Any>?
    ): Driver.OperationResult {
        val uuid = UUID.randomUUID().toString()
        storage.putIfAbsent(uuid, data)

        return Driver.OperationResult(
            UUID.randomUUID().toString(),
            Driver.OperationState.SUCCESS,
            emptyArray(),
            emptyMap(),
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
        options: Map<String, Any>?
    ): Driver.OperationResult {
        val fragmentAux: String = fragment ?: throw DataCouldNotBeFoundException()
        if (!storage.contains(fragmentAux)) {
            throw DataCouldNotBeFoundException()
        }
        storage.replace(fragmentAux, data)

        return Driver.OperationResult(
            UUID.randomUUID().toString(),
            Driver.OperationState.SUCCESS,
            emptyArray(),
            emptyMap(),
            fragmentAux,
            null,
            null
        )
    }

    override fun read(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): ByteArray {
        val fragmentAux: String = fragment ?: throw DataCouldNotBeFoundException()
        return storage[fragmentAux] ?: throw DataCouldNotBeFoundException()
    }

    override fun delete(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        options: Map<String, Any>?
    ) {
        val fragmentAux: String = fragment ?: throw DataCouldNotBeFoundException()
        storage.remove(fragmentAux)
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