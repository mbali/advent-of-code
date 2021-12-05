import kotlin.math.max
import kotlin.math.min

private data class Position(val x: Int, val y: Int)

private class Line(val start: Position, val end: Position) {
    fun positions(ordinalDirectionsOnly: Boolean): List<Position> {
        return if (start.x == end.x) {
            (min(start.y, end.y)..max(start.y, end.y)).map { y -> Position(start.x, y) }
        } else if (start.y == end.y) {
            (min(start.x, end.x)..max(start.x, end.x)).map { x -> Position(x, start.y) }
        } else if (ordinalDirectionsOnly) {
            return emptyList()
        } else {
            TODO()
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

fun main() {

    fun part1(input: List<String>): Int {
        return input.map(Line::from)
            .flatMap { it.positions(true) }
            .groupingBy { it }
            .eachCount()
            .count() { it.value > 1 }
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == 5)

    val input = readInput("Day05")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
