package drivers

import fmgp.did.method.prism.*
import fmgp.did.method.prism.cardano.*
import fmgp.did.method.prism.vdr.*
import interfaces.Driver
import interfaces.Proof
import java.security.PublicKey
import java.util.UUID
import kotlin.emptyArray
import org.hyperledger.identus.apollo.derivation.HDKey
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PrivateKey

/**
 * InMemoryDriver is an example implementation of the [Driver] interface. It stores data in a
 * mutable map in memory. This implementation is useful for testing or scenarios where persistent
 * storage is not required.
 *
 * @property storage A mutable map that holds the stored data.
 */
class PRISMDriver(
        val bfConfig: BlockfrastConfig,
        wallet: CardanoWalletConfig,
) : Driver {

  override val identifier: String = ""
  override val family: String = ""
  override val version: String = "1.0"
  override val supportedVersions: Array<String> = emptyArray()

  val workdir: String = "../../prism-vdr/mainnet"
  val didPrism: DIDPrism =
          DIDPrism("51d47b13393a7cc5c1afc47099dcbecccf0c8a70828c072ac82f55225b42d4f4")
  val keyName: String = "vdr1"

  val seed =
          MnemonicHelper.Companion.createSeed(
                  listOf(
                          "mention",
                          "side",
                          "album",
                          "physical",
                          "uncle",
                          "lab",
                          "horn",
                          "nasty",
                          "script",
                          "few",
                          "hazard",
                          "announce",
                          "upon",
                          "group",
                          "ten",
                          "moment",
                          "fantasy",
                          "helmet",
                          "supreme",
                          "early",
                          "gadget",
                          "curve",
                          "lecture",
                          "edge"
                  ),
                  ""
          )

  val vdrKey: KMMECSecp256k1PrivateKey = HDKey(seed, 0, 1).getKMMSecp256k1PrivateKey()

  private /*lateinit*/ var genericVDRDriver: GenericVDRDriver

  init {
    genericVDRDriver =
            GenericVDRDriver(
                    bfConfig,
                    wallet,
                    workdir, // : String = "../../prism-vdr/mainnet",
                    didPrism,
                    keyName,
                    vdrKey
            )

    genericVDRDriver.initState()
    // TODO
    // initialiseVDRIndexer()
  }

  fun test() = ""

  /** Exception thrown when data cannot be found in the storage. */
  class DataCouldNotBeFoundException : Exception("Could not find the data")

  var storage: MutableMap<String, ByteArray> = mutableMapOf()

  override fun create(data: ByteArray, options: Map<String, Any>?): Driver.OperationResult {
    val uuid = UUID.randomUUID().toString()
    storage += mutableMapOf(Pair(uuid, data))

    return Driver.OperationResult(
            UUID.randomUUID().toString(),
            Driver.OperationState.SUCCESS,
            emptyArray(),
            emptyMap(),
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
    if (fragment != null) {
      storage += mutableMapOf(Pair(fragment, data))

      return Driver.OperationResult(
              UUID.randomUUID().toString(),
              Driver.OperationState.SUCCESS,
              emptyArray(),
              emptyMap(),
              fragment,
              null,
              null
      )
    } else {
      throw DataCouldNotBeFoundException()
    }
  }

  override fun read(
          paths: Array<String>,
          queries: Map<String, String>,
          fragment: String?,
          publicKeys: Array<PublicKey>?
  ): ByteArray {
    if (fragment != null) {
      return storage.getValue(fragment)
    } else {
      throw DataCouldNotBeFoundException()
    }
  }

  override fun delete(
          paths: Array<String>,
          queries: Map<String, String>,
          fragment: String?,
          options: Map<String, Any>?
  ) {
    if (fragment != null) {
      storage.remove(fragment)
    } else {
      throw DataCouldNotBeFoundException()
    }
  }

  override fun verify(
          paths: Array<String>,
          queries: Map<String, String>,
          fragment: String?,
          publicKeys: Array<PublicKey>?,
          returnData: Boolean
  ): Proof {
    val dataRead = read(paths, queries, fragment, publicKeys)
    val sendData =
            if (returnData) {
              dataRead
            } else {
              null
            }
    return Proof(
            "SHA256",
            sendData,
            ByteArray(0) // Empty SHA256 for now we dont want to implement
    )
  }

  override fun storeResultState(identifier: String): Driver.OperationState {
    return Driver.OperationState.SUCCESS
  }
}
