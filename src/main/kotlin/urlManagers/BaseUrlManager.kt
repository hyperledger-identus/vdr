package urlManagers

import interfaces.URLManager
import java.net.URI
import java.security.PublicKey
import java.util.Base64

/**
 * LocalhostUrlManager is an example implementation of the [URLManager] interface.
 * It constructs and resolves URLs based on a given base URL, path segments, queries, and fragments.
 *
 * @property baseURL The base URL used in constructing complete URLs.
 * @property type A string representing the type of URL manager. Default is "Localhost".
 */
class BaseUrlManager(
    val baseURL: String,
    override val type: String = "BaseURL"
): URLManager {

    override fun create(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?
    ): String {
        val finalQueries = queries.toMutableMap()
        val pkQueries = publicKeys?.toBase64UrlMap()
        if (pkQueries != null) {
            finalQueries += pkQueries
        }
        val queryString = finalQueries.entries.joinToString("&") {
            "${it.key}=${it.value}"
        }

        val path = paths.joinToString("/")

        val url = if (fragment.isNullOrEmpty()) {
            "$baseURL/$path?$queryString"
        } else {
            "$baseURL/$path?$queryString#$fragment"
        }

        return url
    }

    override fun resolve(url: String): URLManager.URL {
        val parsedUrl = URI(url)

        val paths = parsedUrl.path.split("/").filter { it.isNotEmpty() }.toTypedArray()

        val queryPairs = mutableMapOf<String, String>()
        parsedUrl.query?.split("&")?.forEach { param ->
            val (key, value) = param.split("=").let { it[0] to it.getOrElse(1) { "" } }
            queryPairs[key] = value
        }

        val fragment = parsedUrl.fragment

        return URLManager.URL(
            paths = paths,
            queries = queryPairs,
            fragment = fragment,
            publicKeys = null
        )
    }

    override fun canResolve(url: String): Boolean = runCatching { resolve(url) }.isSuccess
}

fun Array<PublicKey>.toBase64UrlMap(): Map<String, String> {
    val enc = Base64.getUrlEncoder().withoutPadding()

    return withIndex().associate { (i, k) ->
        "pk${i + 1}" to enc.encodeToString(k.encoded)
    }
}