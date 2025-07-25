package org.hyperledger.identus.vdr.proxy

import org.hyperledger.identus.vdr.interfaces.Driver
import org.hyperledger.identus.vdr.interfaces.Proof
import org.hyperledger.identus.vdr.interfaces.URLManager
import org.hyperledger.identus.vdr.interfaces.VDR

/**
 * VDRProxyMultiDrivers is an implementation of the [VDR] interface that acts as a proxy,
 * connecting multiple [Driver] implementations with a [URLManager] to provide a unified API.
 * It delegates storage, retrieval, and removal operations to the appropriate driver based on provided metadata.
 *
 * @property urlManager The URL manager used for constructing and resolving URLs.
 * @property drivers An array of available drivers.
 * @property identifier A unique identifier for the VDR proxy instance.
 * @property version The version of the VDR proxy implementation.
 */
class VDRProxyMultiDrivers(
    val urlManager: URLManager,
    var drivers: Array<Driver>,
    override val identifier: String,
    override val version: String
): VDR {

    /**
     * Exception thrown when no drivers are available.
     */
    class NoDriversException: Exception("The VDR does not have any driver associated")

    /**
     * Exception thrown when no driver matches the specified identifier or type.
     *
     * @param driverIdentifier The requested driver identifier.
     * @param driverType The requested driver type.
     * @param availableDrivers An array of available drivers (identifier and type pairs).
     */
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

    override fun create(data: ByteArray, options: Map<String, Any>): String {
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }

        val operationResult: Driver.OperationResult
        val identifier: String
        val type: String
        val version: String
        if (drivers.size == 1) {
            val driver = drivers.first()
            identifier = driver.identifier
            type = driver.family
            version = driver.version
            operationResult = driver.create(data, options)
        } else {
            val driver = processDriver(options)
            identifier = driver.identifier
            type = driver.family
            version = driver.version
            operationResult = driver.create(data, options)
        }

        val isMutable = if (options.isMutable()) "1" else "0"

        return urlManager.create(
            operationResult.paths,
            mapOf(Pair("drf", type), Pair("drid", identifier), Pair("drv", version), Pair("m", isMutable)) + operationResult.queries,
            operationResult.fragment,
            operationResult.publicKeys
        )
    }

    override fun update(data: ByteArray, url: String, options: Map<String, Any>): String? {
        val resolvedUrl = urlManager.resolve(url)
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }
        val operationResult: Driver.OperationResult
        val identifier: String
        val type: String
        val version: String
        if (drivers.size == 1) {
            val driver = drivers.first()
            identifier = driver.identifier
            type = driver.family
            version = driver.version
            operationResult = driver.update(
                data,
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                options
            )
        } else {
            val driver = processDriver(resolvedUrl.queries)
            identifier = driver.identifier
            type = driver.family
            version = driver.version
            operationResult = driver.update(
                data,
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                options
            )
        }

        val newURL = urlManager.create(
            operationResult.paths,
            mapOf(Pair("drf", type), Pair("drid", identifier), Pair("drv", version)) + operationResult.queries,
            operationResult.fragment,
            operationResult.publicKeys
        )

        if (url == newURL) {
            return null
        } else {
            return newURL
        }
    }

    override fun read(url: String): ByteArray {
        val resolvedUrl = urlManager.resolve(url)
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }
        if (drivers.size == 1) {
            val driver = drivers.first()
            return driver.read(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                resolvedUrl.publicKeys
            )
        } else {
            val driver = processDriver(resolvedUrl.queries)
            return driver.read(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                resolvedUrl.publicKeys
            )
        }
    }

    override fun delete(url: String, options: Map<String, Any>) {
        val resolvedUrl = urlManager.resolve(url)
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }
        if (drivers.size == 1) {
            val driver = drivers.first()
            driver.delete(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                options
            )
        } else {
            val driver = processDriver(resolvedUrl.queries)
            driver.delete(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                options
            )
        }
    }

    override fun verify(url: String, returnData: Boolean): Proof {
        val resolvedUrl = urlManager.resolve(url)
        if (drivers.isEmpty()) {
            throw NoDriversException()
        }
        if (drivers.size == 1) {
            val driver = drivers.first()
            return driver.verify(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                resolvedUrl.publicKeys,
                returnData
            )
        } else {
            val driver = processDriver(resolvedUrl.queries)
            return driver.verify(
                resolvedUrl.paths,
                resolvedUrl.queries,
                resolvedUrl.fragment,
                resolvedUrl.publicKeys,
                returnData
            )
        }
    }

    /**
     * Selects the appropriate driver based on the provided metadata.
     *
     * @param metadata A map containing keys like "drid" (driver identifier) and "drf" (driver family).
     * @return The matching [Driver] implementation.
     * @throws NoDriverWithThisSpecificationsException if no matching driver is found.
     */
    private fun processDriver(metadata: Map<String, Any>): Driver {
        val driverIdentifier = metadata["drid"] as? String
        val driverType = metadata["drf"] as? String

        when {
            driverIdentifier != null && driverType != null -> {
                return  drivers.firstOrNull {
                    it.identifier == driverIdentifier &&
                            it.family == driverType
                } ?: throw NoDriverWithThisSpecificationsException(
                    driverIdentifier,
                    driverType,
                    drivers.map { Pair(it.identifier, it.family) }.toTypedArray()
                )
            }
            driverType != null -> {
                return drivers.firstOrNull {
                    it.family == driverType
                } ?: throw NoDriverWithThisSpecificationsException(
                    null,
                    driverType,
                    drivers.map { Pair(it.identifier, it.family) }.toTypedArray()
                )
            }
            driverIdentifier != null -> {
                return  drivers.firstOrNull {
                    it.identifier == driverIdentifier
                } ?: throw NoDriverWithThisSpecificationsException(
                    driverIdentifier,
                    null,
                    drivers.map { Pair(it.identifier, it.family) }.toTypedArray()
                )
            }
            else -> {
                throw NoDriverWithThisSpecificationsException(
                    null,
                    null,
                    drivers.map { Pair(it.identifier, it.family) }.toTypedArray()
                )
            }
        }
    }

    private fun Map<String, *>.driverIdentifier(): String? =
        (this["drid"] as? String)?.takeUnless { it.isBlank() }

    private fun Map<String, *>.driverFamily(): String? =
        (this["drf"] as? String)?.takeUnless { it.isBlank() }

    private fun Map<String, *>.driverVersion(): String? =
        (this["drv"] as? String)?.takeUnless { it.isBlank() }

    private fun Map<String, *>.isMutable(): Boolean = this["m"] as? Boolean ?: false
}