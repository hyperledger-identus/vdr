package interfaces

import java.security.PrivateKey
import java.security.PublicKey

/**
 * The Driver interface defines the contract for storage plugins. These drivers can be implemented
 * using various technologies (e.g., databases, blockchains, in-memory storage) and are responsible
 * for the actual data operations.
 */
interface Driver {

    /**
     * Enum representing the operation state after executing a storage operation.
     */
    enum class OperationState {
        RUNNING,
        SUCCESS,
        ERROR
    }

    /**
     * Data class representing the result of a storage operation.
     *
     * @property identifier A unique identifier for the stored data.
     * @property state The state of the operation.
     * @property paths An array of strings representing the storage paths.
     * @property queries A map of query parameters used in constructing the URL.
     * @property fragment An optional URL fragment.
     * @property publicKeys An optional array of public keys related to the stored data.
     */
    data class OperationResult(
        val identifier: String,
        val state: OperationState,
        val paths: Array<String>,
        val queries: Map<String, String>,
        val fragment: String?,
        val publicKeys: Array<PublicKey>?,
        val error: Error?
    )

    /**
     * A unique identifier for the driver instance.
     */
    val identifier: String

    /**
     * The type of driver (e.g., "database", "blockchain", "inMemory").
     */
    val family: String

    /**
     * The version of the driver implementation.
     */
    val version: String

    /**
     * Array of supported versions for compatibility.
     */
    val supportedVersions: Array<String>

    /**
     * Creates data using the driver.
     *
     * @param data The data to be stored as a byte array.
     * @param metadata Optional metadata for the storage operation.
     * @return A [OperationResult] containing the details and state of the storage operation.
     */
    fun create(
        data: ByteArray,
        metadata: Map<String, String>?
    ): OperationResult

    /**
     * Update the stored data.
     *
     * @param data The new data as a byte array.
     * @param paths An array representing the storage paths.
     * @param queries A map of query parameters.
     * @param fragment An optional URL fragment.
     * @param metadata Optional metadata for the operation.
     * @return A [OperationResult] containing the details and state of the mutation operation.
     */
    fun update(
        data: ByteArray,
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        metadata: Map<String, String>?
    ): OperationResult

    /**
     * Retrieves data from the driver.
     *
     * @param paths An array representing the storage paths.
     * @param queries A map of query parameters.
     * @param fragment An optional URL fragment.
     * @param publicKeys An optional array of public keys to verify or decrypt the data.
     * @return The stored data as a [ByteArray].
     */
    fun read(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): ByteArray

    /**
     * Deletes data from the driver.
     *
     * @param paths An array representing the storage paths.
     * @param queries A map of query parameters.
     * @param fragment An optional URL fragment.
     * @param metadata Optional metadata for the removal operation.
     */
    fun delete(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        metadata: Map<String, String>?
    )

    /**
     * Verifies the integrity and authenticity of stored data.
     *
     * The method performs cryptographic operations to generate a [Proof] object which may
     * include a hash or digital signature, depending on whether the data is immutable or mutable.
     *
     * @param paths An array representing the storage paths.
     * @param queries A map of query parameters.
     * @param fragment An optional URL fragment.
     * @param publicKeys An optional array of public keys to verify or decrypt the data.
     * @param returnData Optional flag indicating if the stored data should also be returned.
     * @return A [Proof] object containing the type, cryptographic proof, and optionally the data.
     */
    fun verify(paths: Array<String>,
               queries: Map<String, String>,
               fragment: String?,
               publicKeys: Array<PublicKey>?,
               returnData: Boolean = false
    ): Proof

    /**
     * Returns the current operation state for a given storage result identifier.
     *
     * @param identifier The unique identifier for the storage operation.
     * @return An [OperationState] indicating the status.
     */
    fun storeResultState(identifier: String): OperationState
}