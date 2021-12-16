sealed class BitPacket(val version: Int, val typeId: Int, val position: Int) {
    class Literal(version: Int, typeId: Int, position: Int, val valueBits: String) :
        BitPacket(version, typeId, position)

    class Operator(version: Int, typeId: Int, position: Int, val subPackets: List<BitPacket>) :
        BitPacket(version, typeId, position)

    companion object {
        fun parse(bits: String, position: Int = 0, offset: Int = 0): Pair<BitPacket?, Int> {
            if (bits.length < position + 11) { //need at least 11 bits for a packet
                return null to position
            }
            val version = bits.substring(position..position + 2).toInt(2)
            val typeId = bits.substring(position + 3..position + 5).toInt(2)
            if (typeId == 4) { // literal
                var nextGroupPosition = position + 6
                val literalBits = buildString {
                    do {
                        val hasMoreGroups = bits[nextGroupPosition] == '1'
                        append(bits.substring(nextGroupPosition + 1..nextGroupPosition + 4))
                        nextGroupPosition += 5
                    } while (hasMoreGroups)
                }
                return Literal(version, typeId, position + offset, literalBits) to nextGroupPosition
            } else {
                val lengthType = bits[position + 6].digitToInt(2)
                if (lengthType == 0) {
                    val subPacketsLength = bits.substring(position + 7..position + 21).toInt(2)
                    val subPacketRepresentation = bits.substring(position + 22..position + 21 + subPacketsLength)
                    val subPackets = buildList {
                        var nextPosition = 0
                        do {
                            val (packet, pos) = parse(subPacketRepresentation, nextPosition, offset + 22)
                            nextPosition = pos
                            packet?.let {
                                add(it)
                            }
                        } while (packet != null)
                    }
                    return Operator(version, typeId, position + offset, subPackets) to position + 22 + subPacketsLength
                } else {
                    val subPacketCount = bits.substring(position + 7..position + 17).toInt(2)
                    var next = position + 18
                    val subPackets = buildList(subPacketCount) {
                        repeat(subPacketCount) {
                            val (packet, pos) = parse(bits, next, offset)
                            add(packet ?: throw IllegalStateException("Could not read all subpackets"))
                            next = pos
                        }
                    }
                    return Operator(version, typeId, position + offset, subPackets) to next
                }
            }
        }
    }
}

fun main() {

    fun hexToBinary(hex: String) =
        hex.map {
            it.digitToInt(16).toString(2).padStart(4, '0')
        }.joinToString("")


    fun part1(input: String): Int {
        val packet = BitPacket.parse(hexToBinary(input)).first!!
        fun score(packet: BitPacket): Int {
            return packet.version + when (packet) {
                is BitPacket.Operator -> packet.subPackets.sumOf { score(it) }
                else -> 0
            }
        }
        return score(packet)
    }

    fun part2(input: String): Int {
        TODO()
    }

    check(part1("8A004A801A8002F478") == 16)
    check(part1("620080001611562C8802118E34") == 12)
    check(part1("C0015000016115A2E0802F182340") == 23)
    check(part1("A0016C880162017C3686B18A3D4780") == 31)

    val input = readInput("Day16").first()
    println(part1(input))

    check(part2(TODO()) == TODO())
    println(part2(input))
}
