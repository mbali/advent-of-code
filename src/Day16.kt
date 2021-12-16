sealed class BitPacket(val version: Int) {
    abstract fun value(): Long

    class Literal(version: Int, val literal: Long) :
        BitPacket(version) {
        override fun value(): Long = literal
    }

    sealed class Operator(version: Int, val subPackets: List<BitPacket>) :
        BitPacket(version) {
        class Sum(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = subPackets.sumOf { it.value() }
        }

        class Product(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = subPackets.fold(1L) { acc, packet -> acc * packet.value() }
        }

        class Min(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = subPackets.minOf { it.value() }
        }

        class Max(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = subPackets.maxOf { it.value() }
        }

        class GreaterThan(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = if (subPackets[0].value() > subPackets[1].value()) 1 else 0
        }

        class LessThan(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = if (subPackets[0].value() < subPackets[1].value()) 1 else 0
        }

        class EqualTo(version: Int, subPackets: List<BitPacket>) :
            Operator(version, subPackets) {
            override fun value(): Long = if (subPackets[0].value() == subPackets[1].value()) 1 else 0
        }

        companion object {
            fun of(version: Int, typeId: Int, subPackets: List<BitPacket>): Operator {
                return when (typeId) {
                    0 -> Sum(version, subPackets)
                    1 -> Product(version, subPackets)
                    2 -> Min(version, subPackets)
                    3 -> Max(version, subPackets)
                    5 -> GreaterThan(version, subPackets)
                    6 -> LessThan(version, subPackets)
                    7 -> EqualTo(version, subPackets)
                    else -> throw IllegalArgumentException("Invalid type id: $typeId")
                }
            }
        }
    }

    companion object {
        fun parse(bits: String, start: Int = 0, upTo: Int = bits.length): Pair<BitPacket?, Int> {
            if (upTo < start + 11) { //need at least 11 bits for a packet
                return null to start
            }
            val version = bits.substring(start..start + 2).toInt(2)
            val typeId = bits.substring(start + 3..start + 5).toInt(2)
            if (typeId == 4) { // literal
                var nextGroupPosition = start + 6
                val literalBits = buildString {
                    do {
                        val hasMoreGroups = bits[nextGroupPosition] == '1'
                        append(bits.substring(nextGroupPosition + 1..nextGroupPosition + 4))
                        nextGroupPosition += 5
                    } while (hasMoreGroups)
                }
                return Literal(version, literalBits.toLong(2)) to nextGroupPosition
            } else {
                val lengthType = bits[start + 6].digitToInt(2)
                if (lengthType == 0) {
                    val subPacketsLength = bits.substring(start + 7..start + 21).toInt(2)
                    val subPackets = buildList {
                        var nextPosition = start + 22
                        do {
                            val (packet, pos) = parse(bits, nextPosition, start + 22 + subPacketsLength)
                            nextPosition = pos
                            packet?.let {
                                add(it)
                            }
                        } while (packet != null)
                    }
                    return Operator.of(
                        version,
                        typeId,
                        subPackets
                    ) to start + 22 + subPacketsLength
                } else {
                    val subPacketCount = bits.substring(start + 7..start + 17).toInt(2)
                    var next = start + 18
                    val subPackets = buildList(subPacketCount) {
                        repeat(subPacketCount) {
                            val (packet, pos) = parse(bits, next, upTo)
                            add(packet ?: throw IllegalStateException("Could not read all subpackets"))
                            next = pos
                        }
                    }
                    return Operator.of(version, typeId, subPackets) to next
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

    fun part2(input: String): Long {
        return BitPacket.parse(hexToBinary(input)).first!!.value()
    }

    check(part1("8A004A801A8002F478") == 16)
    check(part1("620080001611562C8802118E34") == 12)
    check(part1("C0015000016115A2E0802F182340") == 23)
    check(part1("A0016C880162017C3686B18A3D4780") == 31)

    val input = readInput("Day16").first()
    println(part1(input))

    check(part2("C200B40A82") == 3L)
    check(part2("04005AC33890") == 54L)
    check(part2("880086C3E88112") == 7L)
    check(part2("CE00C43D881120") == 9L)
    check(part2("D8005AC2A8F0") == 1L)
    check(part2("F600BC2D8F") == 0L)
    check(part2("9C005AC2F8F0") == 0L)
    check(part2("9C0141080250320F1802104A08") == 1L)
    println(part2(input))
}
