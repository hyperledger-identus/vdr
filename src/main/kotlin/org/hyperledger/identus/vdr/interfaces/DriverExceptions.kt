package org.hyperledger.identus.vdr.interfaces

/**
 * Documented error contract for [Driver] data-access operations.
 *
 * Unlike [Driver.create], which reports failures via [Driver.OperationResult.state],
 * [Driver.read], [Driver.update], and [Driver.delete] signal failures by throwing exceptions.
 * This lets HTTP bindings and other integrators map outcomes to status codes (for example,
 * 404 when data is missing or deactivated).
 *
 * ### Built-in drivers
 *
 * [org.hyperledger.identus.vdr.drivers.DatabaseDriver] and
 * [org.hyperledger.identus.vdr.drivers.InMemoryDriver] throw a nested
 * [org.hyperledger.identus.vdr.drivers.DatabaseDriver.DataCouldNotBeFoundException] when:
 * - the URL [fragment][Driver.read] is null or absent,
 * - [Driver.read] / [Driver.delete] target a non-existent entry, or
 * - [Driver.update] affects zero rows.
 *
 * ### PRISM driver
 *
 * The [PRISM VDR driver](https://github.com/hyperledger-identus/prism-vdr-driver) throws
 * additional types so callers can distinguish uninitialized versus deactivated entries:
 * - `hyperledger.identus.vdr.prism.DataCouldNotBeFoundException` — invalid path or missing identifier
 * - `hyperledger.identus.vdr.prism.DataNotInitializedException` — entry exists but has no payload yet
 * - `hyperledger.identus.vdr.prism.DataAlreadyDeactivatedException` — entry was deactivated
 * - `hyperledger.identus.vdr.prism.DataOfUnexpectedTypeException` — unsupported payload type for the operation
 *
 * @see Driver.read
 * @see Driver.update
 * @see Driver.delete
 */
object DriverExceptions
