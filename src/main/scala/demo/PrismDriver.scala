package demo

import scala.jdk.CollectionConverters.*
import fmgp.did.method.prism.*
import fmgp.did.method.prism.cardano.*
import fmgp.did.method.prism.vdr.*
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PrivateKey
import interfaces.Driver
import interfaces.Proof
import interfaces.Driver.OperationState

object PRISMDriver {

  val didPrism: DIDPrism = DIDPrism("51d47b13393a7cc5c1afc47099dcbecccf0c8a70828c072ac82f55225b42d4f4")
  val keyName: String = "vdr1"

  val wallet = CardanoWalletConfig(
    Seq(
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
    )
  )

  private val vdrKey: KMMECSecp256k1PrivateKey =
    wallet.secp256k1PrivateKey(0, 1)

  def default = PRISMDriver(
    bfConfig = BlockfrastConfig(token = "preprod9EGSSMf6oWb81qoi8eW65iWaQuHJ1HwB"),
    wallet = wallet,
    didPrism = didPrism,
    vdrKey = vdrKey,
    keyName = "vdr1",
    workdir = "../../prism-vdr/mainnet",
  )
}

case class PRISMDriver(
    val bfConfig: BlockfrastConfig,
    wallet: CardanoWalletConfig,
    didPrism: DIDPrism,
    vdrKey: KMMECSecp256k1PrivateKey,
    keyName: String = "vdr1",
    workdir: String = "../../prism-vdr/mainnet"
) extends Driver {
  // implement methods here (or use stub for now)
  def getFamily(): String = "PRISM" //   override val family = "prism"
  def getIdentifier(): String = "id" //   override val identifier = "id"
  def getVersion(): String = "1.0" //   override val version = "1.0"
  def getSupportedVersions(): Array[String] = Array("1.0")

  private val genericVDRDriver: GenericVDRDriver =
    GenericVDRDriver(
      bfConfig,
      wallet,
      workdir = workdir,
      didPrism,
      keyName,
      vdrKey,
      maybeMsgCIP20 = Some("PRISMDriver"),
    )

  genericVDRDriver.initState
  // TODO check the status of the SSI and the key

  override def create(
      data: Array[Byte],
      options: java.util.Map[String, ?]
  ): interfaces.Driver.OperationResult = {
    GenericVDRDriver.runProgram(
      for {
        ret <- genericVDRDriver.createBytesEntry(data)
        out = Driver.OperationResult(
          ret._1.value,
          Driver.OperationState.SUCCESS,
          Array(ret._1.value),
          Map(("h", ret._1.value)).asJava,
          null,
          null,
          null
        )
      } yield out
    )
  }

  override def update(
      data: Array[Byte],
      paths: Array[String],
      queries: java.util.Map[String, String],
      fragment: String,
      options: java.util.Map[String, ?]
  ): interfaces.Driver.OperationResult = {
    paths.headOption match
      case None => ??? // interfaces.Driver.OperationResult
      case Some(hash) =>
        val eventRef: RefVDR = RefVDR(hash)
        GenericVDRDriver.runProgram(
          for {
            ret <- genericVDRDriver.updateBytesEntry(eventRef, data)
            out = Driver.OperationResult(
              ret._1.value, // FIXME the identifier MUST be the hash of this event. Not the create one
              Driver.OperationState.SUCCESS,
              Array(ret._1.value),
              Map(("h", ret._1.value)).asJava,
              null,
              null,
              null
            )
          } yield out
        )
  }

  override def delete(
      paths: Array[String],
      queries: java.util.Map[String, String],
      fragment: String,
      options: java.util.Map[String, ?]
  ): Unit = {
    paths.headOption match
      case None => ??? // interfaces.Driver.OperationResult
      case Some(hash) =>
        val eventRef: RefVDR = RefVDR(hash)
        GenericVDRDriver.runProgram(
          for {
            ret <- genericVDRDriver.deleteBytesEntry(eventRef)
            out = Driver.OperationResult(
              ret._1.value, // FIXME the identifier MUST be the hash of this event. Not the create one
              Driver.OperationState.SUCCESS,
              Array(ret._1.value),
              Map(("h", ret._1.value)).asJava,
              null,
              null,
              null
            )
          } yield out
        )
  }

  override def read(
      paths: Array[String],
      queries: java.util.Map[String, String],
      fragment: String,
      publicKeys: Array[java.security.PublicKey]
  ): Array[Byte] = {
    paths.headOption match
      case None => Array.empty()
      case Some(hash) =>
        val eventRef: RefVDR = RefVDR(hash)
        GenericVDRDriver.runProgram(
          for {
            vdr <- genericVDRDriver.read(eventRef)
          } yield vdr.data match {
            case VDR.DataEmpty()              => Array.empty()
            case VDR.DataDeactivated(data)    => Array.empty()
            case VDR.DataByteArray(byteArray) => byteArray
            case VDR.DataIPFS(cid)            => Array.empty()
            case VDR.DataStatusList(status)   => Array.empty()
          }
        )
  }

  /** in the case of PRISM this identifier is the HASH of the event */
  override def storeResultState(
      identifier: String
  ): interfaces.Driver.OperationState = // TODO
    OperationState.SUCCESS // FIXME
    // OperationState.RUNNING //FIXME since everything is a synchronous, the driver needs to keep a internal state for this
    // OperationState.ERROR //FIXME only makes sense when the event submitted is not the latest version or if the submission didn't end up the the blockchain

  override def verify(
      paths: Array[String],
      queries: java.util.Map[String, String],
      fragment: String,
      publicKeys: Array[java.security.PublicKey],
      returnData: Boolean
  ): interfaces.Proof = {
    paths.headOption match
      case None => ???
      case Some(hash) =>
        val eventRef: RefVDR = RefVDR(hash)
        GenericVDRDriver.runProgram(
          for {
            vdr <- genericVDRDriver.read(eventRef)
          } yield vdr.data match {
            case VDR.DataEmpty() => ???
            case VDR.DataDeactivated(data) =>
              data match {
                case VDR.DataEmpty()           => ???
                case VDR.DataDeactivated(data) => ??? // ERROR
                case VDR.DataByteArray(byteArray) =>
                  Proof(
                    "PrismBlock",
                    byteArray, // Data
                    Array
                      .empty() // TODO proof will is a protobuf Array of PRISM events. Should we reuse the PrismBlock?
                  )
                case VDR.DataIPFS(cid)          => ???
                case VDR.DataStatusList(status) => ???
              }
            case VDR.DataByteArray(byteArray) =>
              Proof(
                "PrismBlock",
                byteArray, // Data
                Array.empty() // TODO proof will is a protobuf Array of PRISM events like a PrismBlock?
              )
            case VDR.DataIPFS(cid)          => ???
            case VDR.DataStatusList(status) => ???
          }
        )
  }

}

object App { // ./gradlew run
  // TODO try with build.sbt https://github.com/JetBrains/sbt-kotlin-plugin
  def main(args: Array[String]): Unit = {
    println("RUN PRISMDriver")
    println(PRISMDriver.default.getVersion)
    val aux = PRISMDriver.default.create("Test PRISMDriver".getBytes, Map.empty.asJava)
    // https://preprod.cardanoscan.io/transaction/774d749d42243d27c308b5e849d3cf9848c05bcd95fa28a3f48f4cfeee1f33bc?tab=metadata
    println(aux) //
  }
}
