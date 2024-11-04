package interfaces

import java.security.PrivateKey
import java.security.PublicKey

interface Driver {
    enum class OperationState {
        RUNNING,
        SUCCESS,
        ERROR
    }

    data class StoreResult(
        val state: OperationState,
        val paths: Array<String>,
        val queries: Map<String, String>,
        val fragment: String?,
        val publicKeys: Array<PublicKey>?
    )

    val identifier: String
    val type: String
    val version: String
    val supportedVersions: Array<String>

    fun store(
        data: ByteArray,
        privateKeys: Array<PrivateKey>?,
        metadata: Map<String, String>?
    ): StoreResult

    fun get(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): ByteArray

    fun remove(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        privateKeys: Array<PrivateKey>?,
        metadata: Map<String, String>?
    )
}