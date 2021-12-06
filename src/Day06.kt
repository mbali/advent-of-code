fun main() {
    fun tick(input: LongArray): LongArray {
        check(input.size == 9) { "Input size should be 9" }
        val result = LongArray(9)
        for (i in 1..8) {
            result[i - 1] = input[i]
        }
        result[8] = input[0] //new spawns
        result[6] += input[0] //reset timers
        return result
    }

    fun solution(input: List<String>, days: Int): Long {
        val initialTimers = input.first().split(',').map { it.toInt() }
        var countsByTimer = LongArray(9) { idx -> initialTimers.count { it == idx }.toLong() }
        repeat(days) {
            countsByTimer = tick(countsByTimer)
        }
        return countsByTimer.sum()
    }

    fun part1(input: List<String>): Long {
        return solution(input, 80)
    }

    fun part2(input: List<String>): Long {
        return solution(input, 256)
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 5_934L)

    val input = readInput("Day06")
    println(part1(input))

    check(part2(testInput) == 26_984_457_539L)
    println(part2(input))
}
