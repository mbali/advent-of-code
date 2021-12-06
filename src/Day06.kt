fun main() {
    fun tick(input: IntArray): IntArray {
        check(input.size == 9) { "Input size should be 9" }
        val result = IntArray(9)
        for (i in 1..8) {
            result[i - 1] = input[i]
        }
        result[8] = input[0] //new spawns
        result[6] += input[0] //reset timers
        return result
    }

    fun part1(input: List<String>): Int {
        val initialTimers = input.first().split(',').map { it.toInt() }
        var countsByTimer = IntArray(9) { idx -> initialTimers.count { it == idx } }
        for (days in 1..80) {
            countsByTimer = tick(countsByTimer)
        }
        return countsByTimer.sum()
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 5934)

    val input = readInput("Day06")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
