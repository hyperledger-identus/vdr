package org.hyperledger.identus.vdr.interfaces

import java.security.PublicKey

/**
 * The URLManager interface is responsible for constructing and resolving URLs.
 * The URLs can represent various types such as DIDs or common web URLs.
 */
interface URLManager {

    /**
     * Data class representing a parsed URL.
     *
     * @property paths An array of strings representing the path segments.
     * @property queries A map of query parameters.
     * @property fragment An optional fragment from the URL.
     * @property publicKeys An optional array of public keys.
     */
    data class URL(
        val paths: Array<String>,
        val queries: Map<String, String>,
        val fragment: String?,
        val publicKeys: Array<PublicKey>?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as URL

            if (!paths.contentEquals(other.paths)) return false
            if (queries != other.queries) return false
            if (fragment != other.fragment) return false
            if (publicKeys != null) {
                if (other.publicKeys == null) return false
                if (!publicKeys.contentEquals(other.publicKeys)) return false
            } else if (other.publicKeys != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = paths.contentHashCode()
            result = 31 * result + queries.hashCode()
            result = 31 * result + (fragment?.hashCode() ?: 0)
            result = 31 * result + (publicKeys?.contentHashCode() ?: 0)
            return result
        }
    }

    /**
     * The type of URL manager (e.g., "Localhost", "DID", etc.).
     */
    val type: String

    /**
     * Creates a new URL based on the provided components.
     *
     * @param paths An array representing the path segments.
     * @param queries A map of query parameters.
     * @param fragment An optional URL fragment.
     * @param publicKeys An optional array of public keys.
     * @return A [String] representing the constructed URL.
     */
    fun create(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): String

    /**
     * Resolves (parses) the given URL string into its components.
     *
     * @param url The URL string to be resolved.
     * @return A [URL] data object containing the parsed path, query, fragment, and public keys.
     */
    fun resolve(url: String): URL

    /**
     * Can resolve (parses) the given URL string into its components.
     *
     * @param url The URL string to be resolved.
     * @return A [Boolean] indicating that this url manager can resolve the url.
     */
    fun canResolve(url: String): Boolean
}