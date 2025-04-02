package urlManagers

import interfaces.URLManager
import java.net.URL
import java.security.PublicKey

/**
 * LocalhostUrlManager is an example implementation of the [URLManager] interface.
 * It constructs and resolves URLs based on a given base URL, path segments, queries, and fragments.
 *
 * @property baseURL The base URL used in constructing complete URLs.
 * @property type A string representing the type of URL manager. Default is "Localhost".
 */
class LocalhostUrlManager(
    val baseURL: String = "http://localhost",
    override val type: String = "Localhost"
): URLManager {

    override fun create(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): String {
        val queryString = queries.entries.joinToString("&") { "${it.key}=${it.value}" }
        val path = paths.joinToString("/")

        val url = if (fragment.isNullOrEmpty()) {
            "$baseURL/$path?$queryString"
        } else {
            "$baseURL/$path?$queryString#$fragment"
        }

        return url
    }

    override fun resolve(url: String): URLManager.URL {
        val parsedUrl = URL(url)

        val paths = parsedUrl.path.split("/").filter { it.isNotEmpty() }.toTypedArray()

        val queryPairs = mutableMapOf<String, String>()
        parsedUrl.query?.split("&")?.forEach { param ->
            val (key, value) = param.split("=").let { it[0] to it.getOrElse(1) { "" } }
            queryPairs[key] = value
        }

        val fragment = parsedUrl.ref

        return URLManager.URL(
            paths = paths,
            queries = queryPairs,
            fragment = fragment,
            publicKeys = null
        )
    }

    override fun canResolve(url: String): Boolean {
        TODO("Not yet implemented")
    }
}