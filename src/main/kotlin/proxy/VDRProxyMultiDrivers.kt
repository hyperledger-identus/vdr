package proxy

import interfaces.Driver
import interfaces.URLManager
import interfaces.VDR

class VDRProxyMultiDrivers(
    val urlManager: URLManager,
    var drivers: Array<Driver>,
    override val identifier: String,
    override val version: String
): VDR {

    class NoDriversException: Exception("The VDR does not have any driver associated")
    class NoDriverWithThisSpecificationsException(
        driverIdentifier: String?,
        driverType: String?,
        availableDrivers: Array<Pair<String, String>>
    ) : Exception(
        "The VDR does not have any driver with identifier: '${driverIdentifier ?: "N/A"}' " +
                "or type: '${driverType ?: "N/A"}'. " +
                "Available drivers are: ${
                    availableDrivers.joinToString("; ") { (id, type) ->
                        "driverIdentifier: $id, driverType: $type"
                    }
                }."
    )

    override fun store(data: ByteArray, metadata: Map<String, String>): String {
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }

        val storeResult: Driver.StoreResult
        val identifier: String
        val type: String
        val version: String
        if (drivers.size == 1) {
            val driver = drivers.first()
            identifier = driver.identifier
            type = driver.type
            version = driver.version
            storeResult = driver.store(data, null, metadata)
        } else {
            val driver = processDriver(metadata)
            identifier = driver.identifier
            type = driver.type
            version = driver.version
            storeResult = driver.store(data, null, metadata)
        }

        return urlManager.createNew(
            storeResult.paths,
            storeResult.queries + mapOf(Pair("dvrId", identifier), Pair("dvrTp", type), Pair("dvrV", version)),
            storeResult.fragment,
            storeResult.publicKeys
        )
    }

    override fun get(url: String): ByteArray {
        val resolvedUrl = urlManager.resolve(url)
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }
        if (drivers.size == 1) {
            val driver = drivers.first()
            return driver.get(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                resolvedUrl.publicKeys
            )
        } else {
            val driver = processDriver(resolvedUrl.queries)
            return driver.get(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                resolvedUrl.publicKeys
            )
        }
    }

    override fun remove(url: String, metadata: Map<String, String>) {
        val resolvedUrl = urlManager.resolve(url)
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }
        if (drivers.size == 1) {
            val driver = drivers.first()
            driver.remove(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                null,
                metadata
            )
        } else {
            val driver = processDriver(resolvedUrl.queries)
            driver.remove(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                null,
                metadata
            )
        }
    }

    private fun processDriver(metadata: Map<String, String>): Driver {
        val driverIdentifier = metadata["dvrId"]
        val driverType = metadata["dvrTp"]

        when {
            driverIdentifier != null && driverType != null -> {
                return  drivers.firstOrNull {
                    it.identifier == driverIdentifier &&
                    it.type == driverType
                } ?: throw NoDriverWithThisSpecificationsException(
                    driverIdentifier,
                    driverType,
                    drivers.map { Pair(it.identifier, it.type) }.toTypedArray()
                )
            }
            driverIdentifier != null -> {
                return  drivers.firstOrNull {
                    it.identifier == driverIdentifier
                } ?: throw NoDriverWithThisSpecificationsException(
                    driverIdentifier,
                    null,
                    drivers.map { Pair(it.identifier, it.type) }.toTypedArray()
                )
            }
            driverType != null -> {
                return  drivers.firstOrNull {
                    it.type == driverType
                } ?: throw NoDriverWithThisSpecificationsException(
                    null,
                    driverType,
                    drivers.map { Pair(it.identifier, it.type) }.toTypedArray()
                )
            }
            else -> {
                throw NoDriverWithThisSpecificationsException(
                    driverIdentifier,
                    driverType,
                    drivers.map { Pair(it.identifier, it.type) }.toTypedArray()
                )
            }
        }
    }
}