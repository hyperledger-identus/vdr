package interfaces

/**
 * The VDR (Verifiable Data Registry) interface defines a proxy for storing, mutating,
 * retrieving, and removing data. Implementations act as a proxy between client calls and
 * underlying storage plugins (drivers) and URL managers.
 */
interface VDR {

    /**
     * A unique identifier for the VDR instance.
     */
    val identifier: String

    /**
     * The version of the VDR implementation.
     */
    val version: String

    /**
     * Creates data in the registry.
     *
     * @param data The data to store as a byte array.
     * @param options A map containing additional metadata.
     * @return A [String] representing the URL or identifier where the data is stored.
     */
    fun create(data: ByteArray, options: Map<String, Any>): String

    /**
     * Updates the data at the given URL.
     *
     * @param data The new data as a byte array.
     * @param url The URL identifier for the data to be mutated.
     * @param options A map containing additional metadata.
     * @return A [String]? representing the new URL or identifier after updates, or null if not applicable.
     */
    fun update(data: ByteArray, url: String, options: Map<String, Any>): String?

    /**
     * Retrieves data from the registry.
     *
     * @param url The URL or identifier of the stored data.
     * @return The stored data as a [ByteArray].
     */
    fun read(url: String): ByteArray

    /**
     * Removes data from the registry.
     *
     * @param url The URL or identifier of the data to be removed.
     * @param options A map containing additional metadata that might be used for logging or verification.
     */
    fun delete(url: String, options: Map<String, Any>)

    /**
     * Verifies the integrity and authenticity of the data associated with the given URL.
     *
     * The method delegates the verification process to the appropriate driver, which performs
     * cryptographic operations to generate a [Proof] object. This proof can include a hash
     * for immutable data or a digital signature for mutable data, and may optionally return the data.
     *
     * @param url The URL or identifier of the stored data.
     * @param returnData Optional flag indicating whether to return the stored data along with the proof.
     * @return A [Proof] object containing the verification details.
     */
    fun verify(url: String, returnData: Boolean = false): Proof
}