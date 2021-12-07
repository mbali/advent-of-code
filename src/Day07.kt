import kotlin.math.absoluteValue

fun main() {

    fun solution(positions: List<Int>, costCalculation: (distance: Int) -> Int): Int {
        val targets = positions.minOf { it }..positions.maxOf { it }
        return targets.map { target -> positions.sumOf { costCalculation((it - target).absoluteValue) } }.minOf { it }
    }

    fun part1(input: List<String>): Int {
        val positions = input.first().split(',').map { it.toInt() }
        return solution(positions) { it }
    }

    fun part2(input: List<String>): Int {
        val positions = input.first().split(',').map { it.toInt() }
        return solution(positions) { it * (it + 1) / 2 }
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37)

    val input = readInput("Day07")
    println(part1(input))

    check(part2(testInput) == 168)
    println(part2(input))
}
