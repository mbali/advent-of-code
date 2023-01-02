package year2022

import readInput

sealed class Day13Packet : Comparable<Day13Packet> {
    data class IntegerPacket(val value: Int) : Day13Packet() {
        private val asList = ListPacket(listOf(this))
        override fun compareTo(other: Day13Packet): Int {
            if (other is IntegerPacket) return value.compareTo(other.value)
            return asList.compareTo(other)
        }

        override fun toString() = value.toString()
    }

    data class ListPacket(val values: List<Day13Packet>) : Day13Packet() {
        override fun compareTo(other: Day13Packet): Int {
            return when (other) {
                is IntegerPacket -> -other.compareTo(this)
                is ListPacket -> values.zip(other.values).map { it.first.compareTo(it.second) }.firstOrNull { it != 0 }
                    ?: values.size.compareTo(other.values.size)
            }
        }

        override fun toString() = values.joinToString(",", "[", "]")
    }

    companion object {
        fun parse(input: String): Day13Packet {
            val tokens = Day13Token.parse(input).listIterator()

            fun peek() = when {
                tokens.hasNext() -> {
                    val token = tokens.next()
                    tokens.previous()
                    token
                }

                else -> null
            }

            fun buildPacket(): Day13Packet {
                if (tokens.hasNext()) {
                    return when (val token = tokens.next()) {
                        is Day13Token.IntToken -> IntegerPacket(token.value)
                        is Day13Token.ListStart -> ListPacket(buildList {
                            while (peek() !is Day13Token.ListEnd) {
                                add(buildPacket())
                            }
                            tokens.next()
                        })

                        is Day13Token.ListEnd -> throw IllegalArgumentException("Unexpected list end")
                    }
                }
                throw IllegalArgumentException("Unexpected end of input")
            }

            return buildPacket()
        }
    }
}

sealed class Day13Token {
    data class IntToken(val value: Int) : Day13Token()
    object ListStart : Day13Token()
    object ListEnd : Day13Token()

    companion object {

        private val tokenRegex = Regex("""\d+|[\[\]]""")

        fun parse(input: String): List<Day13Token> =
            buildList {
                val tokens = tokenRegex.findAll(input)
                tokens.forEach { token ->
                    when (val value = token.value) {
                        "[" -> add(ListStart)
                        "]" -> add(ListEnd)
                        else -> add(IntToken(value.toInt()))
                    }
                }
            }
    }
}

private fun String.asPacket() = Day13Packet.parse(this)

object Day13 {

    private fun packets(input: List<String>) =
        input.filter(String::isNotEmpty).map(String::asPacket)

    private fun packetPairs(input: List<String>) =
        packets(input).chunked(2).map { (p1, p2) -> p1 to p2 }


    fun part1Indices(input: List<String>) = packetPairs(input).mapIndexedNotNull { index, (packet1, packet2) ->
        when {
            packet1 < packet2 -> index + 1
            else -> null
        }
    }
}

fun main() {

    val inputClassifier = "Day13"

    fun part1(input: List<String>): Int = Day13.part1Indices(input).sum()

    fun part2(input: List<String>): Int {
        val dividers = setOf("[[2]]", "[[6]]")
        val packets = buildList {
            addAll(dividers)
            addAll(input.filter { it.isNotEmpty() })
        }

        return packets.sortedBy(String::asPacket).mapIndexedNotNull { idx, packet ->
            when (packet) {
                in dividers -> idx + 1
                else -> null
            }
        }.reduce(Int::times)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 13)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 140)
    println(part2(input))

}
