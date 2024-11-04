package urlManagers

import interfaces.URLManager
import java.net.URL
import java.security.PublicKey

class LocalhostUrlManager(
    val baseURL: String = "http://localhost",
    override val type: String = "Localhost"
): URLManager {
    override fun createNew(
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
}