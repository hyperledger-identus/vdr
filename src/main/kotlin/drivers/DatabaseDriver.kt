package drivers

import interfaces.Driver
import interfaces.Proof
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64
import java.util.UUID
import javax.sql.DataSource

/**
 * DatabaseDriver is a JDBC‑backed implementation of the [Driver] interface.
 * It persists data in a relational database table called `storage`.
 *
 * The table schema is created on first initialisation if it does not yet exist:
 * ```sql
 * CREATE TABLE IF NOT EXISTS storage (
 *   id VARCHAR(64) PRIMARY KEY,
 *   data BLOB NOT NULL
 * );
 * ```
 *
 * The constructor expects a configured [DataSource] so that callers are free to
 * choose the underlying database (PostgreSQL, H2, SQLite, MySQL …) and connection‑pool
 * implementation (e.g. HikariCP) that best fits their runtime.
 */
class DatabaseDriver(
    override val identifier: String,
    override val version: String,
    override val supportedVersions: Array<String>,
    private val dataSource: DataSource
) : Driver {
    class DataCouldNotBeFoundException : Exception("Could not find the data")

    override val family: String = "database"

    init { initialiseSchema() }

    private fun initialiseSchema() {
        dataSource.connection.use { conn ->
            conn.setAutoCommit(true)
            conn.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS storage (
                    id VARCHAR(64) PRIMARY KEY,
                    data BYTEA NOT NULL
                )
                """.trimIndent()
            ).executeUpdate()
        }
    }

    override fun create(data: ByteArray, options: Map<String, Any>?): Driver.OperationResult {
        val uuid = UUID.randomUUID().toString()
        dataSource.connection.use { conn ->
            conn.setAutoCommit(true)
            conn.prepareStatement("INSERT INTO storage(id, data) VALUES(?, ?)").use { ps ->
                ps.setString(1, uuid)
                ps.setBytes(2, data)
                ps.executeUpdate()
            }
        }
        val enc = Base64.getUrlEncoder().withoutPadding()
        return Driver.OperationResult(
            UUID.randomUUID().toString(),
            Driver.OperationState.SUCCESS,
            emptyArray(),
            mapOf(Pair("h", enc.encodeToString(sha256(data)))),
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
        val id = fragment ?: throw DataCouldNotBeFoundException()
        val rowsUpdated = dataSource.connection.use { conn ->
            conn.setAutoCommit(true)
            conn.prepareStatement("UPDATE storage SET data = ? WHERE id = ?").use { ps ->
                ps.setBytes(1, data)
                ps.setString(2, id)
                ps.executeUpdate()
            }
        }
        if (rowsUpdated == 0) throw DataCouldNotBeFoundException()
        val enc = Base64.getUrlEncoder().withoutPadding()
        return Driver.OperationResult(
            UUID.randomUUID().toString(),
            Driver.OperationState.SUCCESS,
            emptyArray(),
            mapOf(Pair("h", enc.encodeToString(sha256(data)))),
            id,
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
        val id = fragment ?: throw DataCouldNotBeFoundException()
        dataSource.connection.use { conn ->
            conn.setAutoCommit(true)
            conn.prepareStatement("SELECT data FROM storage WHERE id = ?").use { ps ->
                ps.setString(1, id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) return rs.getBytes(1)
                }
            }
        }
        throw DataCouldNotBeFoundException()
    }

    override fun delete(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        options: Map<String, Any>?
    ) {
        val id = fragment ?: throw DataCouldNotBeFoundException()
        val rowsDeleted = dataSource.connection.use { conn ->
            conn.setAutoCommit(true)
            conn.prepareStatement("DELETE FROM storage WHERE id = ?").use { ps ->
                ps.setString(1, id)
                ps.executeUpdate()
            }
        }
        if (rowsDeleted == 0) throw DataCouldNotBeFoundException()
    }

    override fun verify(
        paths: Array<String>,
        queries: Map<String, String>,
        fragment: String?,
        publicKeys: Array<PublicKey>?,
        returnData: Boolean
    ): Proof {
        val dataRead = read(paths, queries, fragment, publicKeys)
        val returnedData = if (returnData) dataRead else null
        return Proof(
            "SHA256",
            returnedData,
            sha256(dataRead)
        )
    }

    // In database we have sync writing so the operation state will always be SUCCESS or the creation will throw
    override fun storeResultState(identifier: String): Driver.OperationState = Driver.OperationState.SUCCESS

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
