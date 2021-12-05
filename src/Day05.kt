import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

private data class Position(val x: Int, val y: Int)

private class Line(val start: Position, val end: Position) {
    fun positions(ordinalDirectionsOnly: Boolean): List<Position> {
        val dx = (end.x - start.x).sign
        val dy = (end.y - start.y).sign
        if (ordinalDirectionsOnly && dx != 0 && dy != 0) return emptyList()
        val length = max(abs(end.x - start.x), abs(end.y - start.y))
        return (0..length).map { distance ->
            Position(
                start.x + dx * distance,
                start.y + dy * distance
            )
        }
    }

    companion object {
        fun from(input: String): Line =
            Regex("""\d+""")
                .findAll(input)
                .map { it.value.toInt() }
                .toList()
                .let {
                    Line(
                        Position(it[0], it[1]),
                        Position(it[2], it[3])
                    )
                }
    }
}

private fun List<String>.solution(ordinalDirectionsOnly: Boolean): Int = map(Line::from)
    .flatMap { it.positions(ordinalDirectionsOnly) }
    .groupingBy { it }
    .eachCount()
    .count() { it.value > 1 }

fun main() {

    fun part1(input: List<String>): Int {
        return input.solution(true)
    }

    fun part2(input: List<String>): Int {
        return input.solution(false)
    }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == 5)

    val input = readInput("Day05")
    println(part1(input))

    check(part2(testInput) == 12)
    println(part2(input))
}
