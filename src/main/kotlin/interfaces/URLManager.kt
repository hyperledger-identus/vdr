package interfaces

import java.security.PublicKey

interface URLManager {

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

    val type: String

    fun createNew(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): String

    fun resolve(url: String): URL
}