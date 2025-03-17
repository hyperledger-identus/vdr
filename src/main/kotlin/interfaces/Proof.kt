package interfaces

data class Proof(
    val type: String,
    val data: ByteArray?,
    val proof: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Proof

        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false
        if (!proof.contentEquals(other.proof)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + proof.contentHashCode()
        return result
    }
}
