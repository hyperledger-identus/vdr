package interfaces

interface VDR {
    val identifier: String
    val version: String

    fun store(data: ByteArray, metadata: Map<String, String>): String
    fun get(url: String): ByteArray
    fun remove(url: String, metadata: Map<String, String>)
}