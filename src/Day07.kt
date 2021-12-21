import kotlin.math.absoluteValue

fun main() {

    fun solution(positions: List<Int>, initialGuess: Int, costCalculation: (distance: Int) -> Int): Int {
        val targets = positions.minOf { it }..positions.maxOf { it }
        var minimumCost = positions.sumOf { costCalculation((initialGuess - it).absoluteValue) }
        var guess = initialGuess
        while (guess in targets) {
            //check to the left
            val leftCost = positions.sumOf { costCalculation((guess - 1 - it).absoluteValue) }
            if (leftCost < minimumCost) {
                guess--
                minimumCost = leftCost
                continue
            }
            val rightCost = positions.sumOf { costCalculation((guess + 1 - it).absoluteValue) }
            if (rightCost < minimumCost) {
                guess++
                minimumCost = rightCost
                continue
            }
            break //local minimum is global minimum
        }
        positions.first()
        return minimumCost
    }

    fun part1(input: List<String>): Int {
        val positions = input.first().split(',').map { it.toInt() }
        return solution(positions, positions.sorted().middle()) { it }
    }

    fun part2(input: List<String>): Int {
        val positions = input.first().split(',').map { it.toInt() }
        return solution(positions, positions.average().toInt()) { it * (it + 1) / 2 }
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37)

    val input = readInput("Day07")
    println(part1(input))

    check(part2(testInput) == 168)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
